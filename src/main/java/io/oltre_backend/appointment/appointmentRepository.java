package io.oltre_backend.appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface appointmentRepository extends JpaRepository<appointment, Long> {
    List<appointment> findByApptDateBetweenAndUser_IdOrderByApptTimeAsc(LocalDate start, LocalDate end, Long userId);

    List<appointment> findByApptDateAndUser_Id(LocalDate apptDate, Long userId);

    Optional<appointment> findByUser_IdAndSourceAndSourceRef(Long userId, AppointmentSource source, String sourceRef);

    List<appointment> findByUser_IdAndSourceAndApptDateBetween(Long userId, AppointmentSource source, LocalDate start, LocalDate end);
}