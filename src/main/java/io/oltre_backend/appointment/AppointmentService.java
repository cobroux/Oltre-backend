package io.oltre_backend.appointment;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class AppointmentService {
    private final AppointmentRepository repo;
    public AppointmentService(AppointmentRepository repo) { this.repo = repo; }

    private AppointmentDTO toDto(Appointment a) {
        return new AppointmentDTO(a.getId(), a.getTitle(), a.getDescription(),
                a.getLocation(), a.getApptDate(), a.getApptTime(), a.getApptType());
    }

    public List<AppointmentDTO> getWeek(LocalDate monday) {
        return repo.findByApptDateBetweenOrderByApptTimeAsc(monday, monday.plusDays(6))
                .stream().map(this::toDto).toList();
    }

    public AppointmentDTO save(AppointmentDTO dto) {
        Appointment a = new Appointment();
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
