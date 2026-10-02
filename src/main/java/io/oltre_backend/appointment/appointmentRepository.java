package io.oltre_backend.appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface appointmentRepository extends JpaRepository<appointment, Long> {
    List<appointment> findByApptDateBetweenAndUser_IdOrderByApptTimeAsc(LocalDate start, LocalDate end, Long userId);
}