package io.oltre_backend.demo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import io.oltre_backend.sport.GarminActivity;
import io.oltre_backend.sport.GarminActivityDTO;
import io.oltre_backend.sport.GarminActivityDetailDTO;
import io.oltre_backend.sport.GarminService;

/** Garmin bouchonné : ~30 activités générées sur 8 semaines, jamais d'appel réseau, jamais de GPS. */
@Service
@Primary
@Profile("demo")
public class DemoGarminService extends GarminService {

    private static final int COUNT = 30;

    public DemoGarminService(@Value("${garmin.service-url}") String url, @Value("${garmin.service-token}") String token) {
        super(url, token);
    }

    @Override public void connect(Long userId, String email, String password) { /* bloqué par DemoGuard */ }
    @Override public boolean isConnected(Long userId) { return true; }
    @Override public void disconnect(Long userId) { /* sans effet */ }

    @Override
    public List<GarminActivityDTO> getActivities(Long userId, String monday, int limit) {
        List<GarminActivityDTO> all = new ArrayList<>();
        for (int i = 0; i < COUNT; i++) {
            GarminActivityDTO dto = new GarminActivityDTO(activity(i));
            if (monday == null || dto.getStartLocal().substring(0, 10).compareTo(monday) >= 0) all.add(dto);
        }
        return all.stream().limit(limit).toList();
    }

    @Override
    public GarminActivityDetailDTO getActivityDetails(Long userId, String activityId) {
        int i = (int) (Long.parseLong(activityId) - 1000);
        GarminActivity a = activity(Math.clamp(i, 0, COUNT - 1));
        GarminActivityDetailDTO d = new GarminActivityDetailDTO();
        d.setActivityId(String.valueOf(a.getId()));
        d.setName(a.getName());
        d.setSportType(a.getSportType());
        d.setStartLocal(a.getStartLocal());
        d.setDistance(a.getDistance());
        d.setDuration(a.getDuration());
        d.setElevationGain(a.getElevationGain());
        d.setElevationLoss(a.getElevationGain());
        d.setCalories(a.getCalories());
        d.setAverageHR(150.0 + i % 10);
        d.setMaxHR(176.0);
        d.setAverageSpeed(a.getAvgSpeed());
        d.setMaxSpeed(a.getMaxSpeed());
        d.setPolyline(List.of()); // aucune trace GPS en démo
        List<GarminActivityDetailDTO.Split> splits = new ArrayList<>();
        for (int k = 0; k < (int) (a.getDistance() / 1000) && a.getDistance() > 0 && !a.getSportType().contains("strength"); k++) {
            GarminActivityDetailDTO.Split s = new GarminActivityDetailDTO.Split();
            s.setDistance(1000.0);
            s.setDuration(1000.0 / a.getAvgSpeed());
            s.setAverageSpeed(a.getAvgSpeed());
            s.setAverageHR(d.getAverageHR());
            splits.add(s);
        }
        d.setSplits(splits);
        d.setExerciseSets(List.of());
        return d;
    }

    /** i = 0 : aujourd'hui ; une séance tous les deux jours, cycle course / vélo / course longue / muscu. */
    private GarminActivity activity(int i) {
        LocalDate day = LocalDate.now(ZoneId.of("Europe/Paris")).minusDays(i * 2L);
        String type; String name; double km; double speed; double climb;
        switch (i % 4) {
            case 0 -> { type = "running"; name = "Footing"; km = 5 + (i % 3); speed = 2.9; climb = 40; }
            case 1 -> { type = "cycling"; name = "Sortie vélo"; km = 30 + i; speed = 7.5; climb = 250; }
            case 2 -> { type = "running"; name = i % 8 == 2 ? "Sortie longue" : "Fractionné"; km = i % 8 == 2 ? 10 + i / 8.0 : 8; speed = 3.1; climb = 90; }
            default -> { type = "strength_training"; name = "Renforcement"; km = 0; speed = 0; climb = 0; }
        }
        GarminActivity a = new GarminActivity();
        a.setId(1000L + i);
        a.setName(name);
        a.setActivityType(typeOf(type));
        a.setStartLocal(LocalDateTime.of(day, java.time.LocalTime.of(7, 30)).toString() + ":00");
        a.setDistance(km * 1000);
        a.setDuration(speed > 0 ? km * 1000 / speed : 2700.0);
        a.setElevationGain(climb);
        a.setAvgSpeed(speed);
        a.setMaxSpeed(speed * 1.4);
        a.setCalories(km > 0 ? km * 60 : 250.0);
        return a;
    }

    private static GarminActivity.ActivityType typeOf(String key) {
        GarminActivity.ActivityType t = new GarminActivity.ActivityType();
        t.setTypeKey(key);
        return t;
    }
}
