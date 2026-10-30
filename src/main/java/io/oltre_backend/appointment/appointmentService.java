package io.oltre_backend.appointment;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import io.oltre_backend.user.UserRepository;

@Service
public class appointmentService {

    private final appointmentRepository repo;
    private final UserRepository userRepository;

    public appointmentService(appointmentRepository repo, UserRepository userRepository) {
        this.repo = repo;
        this.userRepository = userRepository;
    }

    private appointmentDTO toDto(appointment a) {
        return new appointmentDTO(a.getId(), a.getTitle(), a.getDescription(),
                a.getLocation(), a.getApptDate(), a.getApptTime(), a.getApptEndTime(), a.getApptType(),
                a.getSource());
    }

    public List<appointmentDTO> getAll(Long userId) {
        return repo.findByUser_Id(userId).stream().map(this::toDto).toList();
    }

    public List<appointmentDTO> getWeek(LocalDate monday, Long userId) {
        return repo.findByApptDateBetweenAndUser_IdOrderByApptTimeAsc(monday, monday.plusDays(6), userId)
                .stream().map(this::toDto).toList();
    }

    public appointmentDTO save(appointmentDTO dto, Long userId) {
        if (dto.getApptTime() != null && dto.getApptEndTime() != null
                && !dto.getApptEndTime().isAfter(dto.getApptTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }

        if (dto.getApptTime() != null) {
            boolean overlaps = repo.findByApptDateAndUser_Id(dto.getApptDate(), userId).stream()
                    .anyMatch(existing -> overlaps(
                            dto.getApptTime(), dto.getApptEndTime(),
                            existing.getApptTime(), existing.getApptEndTime()));
            if (overlaps) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "This appointment overlaps with an existing one");
            }
        }

        appointment a = new appointment();
        a.setTitle(dto.getTitle());
        a.setDescription(dto.getDescription());
        a.setLocation(dto.getLocation());
        a.setApptDate(dto.getApptDate());
        a.setApptTime(dto.getApptTime());
        a.setApptEndTime(dto.getApptEndTime());
        a.setApptType(dto.getApptType());
        a.setUser(userRepository.getReferenceById(userId));
        return toDto(repo.save(a));
    }

    private boolean overlaps(LocalTime newStart, LocalTime newEnd, LocalTime existingStart, LocalTime existingEnd) {
        if (existingStart == null) {
            return false;
        }
        LocalTime a1 = newStart;
        LocalTime a2 = newEnd != null ? newEnd : newStart;
        LocalTime b1 = existingStart;
        LocalTime b2 = existingEnd != null ? existingEnd : existingStart;
        return a1.isBefore(b2) && b1.isBefore(a2);
    }

    public void delete(Long id, Long userId) {
        appointment a = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (a.getUser() == null || !a.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        repo.deleteById(id);
    }

    // Édition manuelle d'un rendez-vous existant. On garde source/sourceRef
    // (sinon la synchro suivante le recréerait en double) et on pose `edited` :
    // la synchro ne doit plus écraser ni supprimer un rendez-vous modifié.
    public appointmentDTO update(Long id, Long userId, appointmentDTO dto) {
        appointment a = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (a.getUser() == null || !a.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (dto.getApptTime() != null && dto.getApptEndTime() != null
                && !dto.getApptEndTime().isAfter(dto.getApptTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }
        if (dto.getApptTime() != null) {
            boolean overlaps = repo.findByApptDateAndUser_Id(dto.getApptDate(), userId).stream()
                    .filter(existing -> !existing.getId().equals(id))
                    .anyMatch(existing -> overlaps(
                            dto.getApptTime(), dto.getApptEndTime(),
                            existing.getApptTime(), existing.getApptEndTime()));
            if (overlaps) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "This appointment overlaps with an existing one");
            }
        }

        a.setTitle(dto.getTitle());
        a.setDescription(dto.getDescription());
        a.setLocation(dto.getLocation());
        a.setApptDate(dto.getApptDate());
        a.setApptTime(dto.getApptTime());
        a.setApptEndTime(dto.getApptEndTime());
        a.setApptType(dto.getApptType());
        a.setEdited(true);
        return toDto(repo.save(a));
    }

    // Crée ou met à jour un rendez-vous généré automatiquement, identifié de
    // façon stable par (userId, source, sourceRef) - appelé à chaque
    // synchronisation des activités Garmin et à chaque génération du plan du
    // Coach IA. Pas de contrôle de chevauchement ici : ce sont des entrées
    // informatives (un fait passé ou une suggestion), pas des créneaux que
    // l'utilisateur bloque volontairement.
    public void upsertFromSource(Long userId, String title, String description, LocalDate apptDate,
                                  LocalTime apptTime, AppointmentSource source, String sourceRef) {
        appointment a = repo.findByUser_IdAndSourceAndSourceRef(userId, source, sourceRef)
                .orElseGet(() -> {
                    appointment created = new appointment();
                    created.setUser(userRepository.getReferenceById(userId));
                    created.setSource(source);
                    created.setSourceRef(sourceRef);
                    created.setApptType(appointmentType.SPORT);
                    return created;
                });
        if (a.isEdited()) return;
        a.setTitle(title);
        a.setDescription(description);
        a.setApptDate(apptDate);
        a.setApptTime(apptTime);
        repo.save(a);
    }

    // Retire les entrées auto-générées d'une source pour une semaine donnée -
    // utilisé avant de réinjecter le plan du Coach IA, pour qu'une
    // régénération qui change un jour (ex: séance retirée) ne laisse pas une
    // entrée périmée dans le calendrier. Les rendez-vous que l'utilisateur a
    // édités entre-temps (`edited`, voir update()) sont conservés.
    public void deleteBySourceForWeek(Long userId, AppointmentSource source, LocalDate weekStart, LocalDate weekEnd) {
        repo.deleteAll(repo.findByUser_IdAndSourceAndApptDateBetween(userId, source, weekStart, weekEnd)
                .stream().filter(a -> !a.isEdited()).toList());
    }
}
