package io.oltre_backend.appointment;
import java.time.LocalDate;
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
                a.getLocation(), a.getApptDate(), a.getApptTime(), a.getApptType());
    }

    public List<appointmentDTO> getWeek(LocalDate monday, Long userId) {
        return repo.findByApptDateBetweenAndUser_IdOrderByApptTimeAsc(monday, monday.plusDays(6), userId)
                .stream().map(this::toDto).toList();
    }

    public appointmentDTO save(appointmentDTO dto, Long userId) {
        appointment a = new appointment();
        a.setTitle(dto.getTitle());
        a.setDescription(dto.getDescription());
        a.setLocation(dto.getLocation());
        a.setApptDate(dto.getApptDate());
        a.setApptTime(dto.getApptTime());
        a.setApptType(dto.getApptType());
        a.setUser(userRepository.getReferenceById(userId));
        return toDto(repo.save(a));
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
