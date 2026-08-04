package io.oltre_backend.appointment;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class appointmentService {
    
    private final appointmentRepository repo;
    
    public appointmentService(appointmentRepository repo) { this.repo = repo; }

    private appointmentDTO toDto(appointment a) {
        return new appointmentDTO(a.getId(), a.getTitle(), a.getDescription(),
                a.getLocation(), a.getApptDate(), a.getApptTime(), a.getApptType());
    }

    public List<appointmentDTO> getWeek(LocalDate monday) {
        return repo.findByApptDateBetweenOrderByApptTimeAsc(monday, monday.plusDays(6))
                .stream().map(this::toDto).toList();
    }

    public appointmentDTO save(appointmentDTO dto) {
        appointment a = new appointment();
        a.setTitle(dto.getTitle());
        a.setDescription(dto.getDescription());
        a.setLocation(dto.getLocation());
        a.setApptDate(dto.getApptDate());
        a.setApptTime(dto.getApptTime());
        a.setApptType(dto.getApptType());
        return toDto(repo.save(a));
    }

    public void delete(Long id) { 
        repo.deleteById(id);
    }
}
