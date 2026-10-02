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
                a.getLocation(), a.getApptDate(), a.getApptTime(), a.getApptEndTime(), a.getApptType());
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
}
