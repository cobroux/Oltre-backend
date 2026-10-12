package io.oltre_backend.sport;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import io.oltre_backend.appointment.AppointmentSource;
import io.oltre_backend.appointment.appointmentService;

// Remplit automatiquement le calendrier à partir des activités Garmin,
// appelé à chaque fois que la liste d'activités est demandée (pas de
// webhook Garmin disponible, donc synchro à la demande plutôt que par
// tâche de fond). Idempotent : chaque activité est identifiée par son id
// Garmin (source=GARMIN, sourceRef="garmin-<id>"), donc un même import
// répété met à jour l'entrée existante au lieu d'en créer une en double.
@Service
public class GarminCalendarSyncService {

    private final appointmentService appointmentService;

    public GarminCalendarSyncService(appointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    public void sync(Long userId, List<GarminActivityDTO> activities) {
        for (GarminActivityDTO a : activities) {
            if (a.getId() == null || a.getStartLocal() == null || a.getStartLocal().length() < 10) continue;

            LocalDate date;
            try {
                date = LocalDate.parse(a.getStartLocal().substring(0, 10));
            } catch (Exception e) {
                continue; // date illisible : on ignore cette activité plutôt que de faire échouer toute la synchro
            }

            appointmentService.upsertFromSource(
                    userId, title(a), description(a), date, parseTime(a.getStartLocal()),
                    AppointmentSource.GARMIN, "garmin-" + a.getId());
        }
    }

    private LocalTime parseTime(String startLocal) {
        // "2026-10-10T07:12:00" ou "2026-10-10 07:12:00" selon la source -
        // on se contente des caractères 11 à 18 ("HH:mm:ss") quel que soit
        // le séparateur de date/heure.
        if (startLocal.length() < 19) return null;
        try {
            return LocalTime.parse(startLocal.substring(11, 19), DateTimeFormatter.ofPattern("HH:mm:ss"));
        } catch (Exception e) {
            return null;
        }
    }

    private String title(GarminActivityDTO a) {
        if (a.getName() != null && !a.getName().isBlank()) return a.getName();
        return sportLabel(a.getSportType());
    }

    private String description(GarminActivityDTO a) {
        StringBuilder sb = new StringBuilder();
        if (a.getDistance() != null && a.getDistance() > 0) {
            sb.append(String.format("%.1f km", a.getDistance() / 1000.0));
        }
        if (a.getMovingTime() != null && a.getMovingTime() > 0) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(a.getMovingTime() / 60).append(" min");
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    private String sportLabel(String sportType) {
        String type = sportType != null ? sportType : "";
        if (type.contains("running")) return "Course à pied";
        if (type.contains("cycling") || type.contains("biking")) return "Vélo";
        if (type.contains("strength")) return "Musculation";
        if (type.contains("swim")) return "Natation";
        if (type.contains("walking") || type.contains("hiking")) return "Marche";
        return "Activité Garmin";
    }
}
