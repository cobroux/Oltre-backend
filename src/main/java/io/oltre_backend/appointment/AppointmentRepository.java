package io.oltre_backend.appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByApptDateBetweenOrderByApptTimeAsc(LocalDate start, LocalDate end);
}
