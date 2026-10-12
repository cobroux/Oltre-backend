package io.oltre_backend.appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.oltre_backend.user.User;
import io.oltre_backend.user.UserRepository;
import org.springframework.web.server.ResponseStatusException;

class AppointmentServiceTest {

    private final appointmentRepository repo = mock(appointmentRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final appointmentService service = new appointmentService(repo, userRepository);

    private static final Long USER_ID = 1L;

    @Test
    void upsertFromSource_noExistingEntry_createsOne() {
        when(repo.findByUser_IdAndSourceAndSourceRef(USER_ID, AppointmentSource.GARMIN, "garmin-42"))
                .thenReturn(Optional.empty());
        when(userRepository.getReferenceById(USER_ID)).thenReturn(new User());
        when(repo.save(any(appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        service.upsertFromSource(USER_ID, "Sortie matinale", "8.2 km", LocalDate.of(2026, 10, 10),
                LocalTime.of(7, 12), AppointmentSource.GARMIN, "garmin-42");

        verify(repo).save(any(appointment.class));
    }

    @Test
    void upsertFromSource_existingEntry_updatesInPlace_noDuplicate() {
        appointment existing = new appointment();
        existing.setId(99L);
        existing.setSource(AppointmentSource.GARMIN);
        existing.setSourceRef("garmin-42");
        existing.setTitle("Ancien titre");

        when(repo.findByUser_IdAndSourceAndSourceRef(USER_ID, AppointmentSource.GARMIN, "garmin-42"))
                .thenReturn(Optional.of(existing));
        when(repo.save(any(appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        service.upsertFromSource(USER_ID, "Sortie matinale (mise à jour)", "8.4 km", LocalDate.of(2026, 10, 10),
                LocalTime.of(7, 12), AppointmentSource.GARMIN, "garmin-42");

        // Jamais de getReferenceById (pas de nouvel appointment créé) et un
        // seul save, sur l'entrée existante (même id) - pas de doublon.
        verify(userRepository, never()).getReferenceById(any());
        verify(repo).save(existing);
        assertEquals("Sortie matinale (mise à jour)", existing.getTitle());
        assertEquals(99L, existing.getId());
    }

    @Test
    void update_byOwner_detachesFromAutoSource() {
        appointment existing = new appointment();
        existing.setId(5L);
        User owner = mock(User.class);
        when(owner.getId()).thenReturn(USER_ID);
        existing.setUser(owner);
        existing.setSource(AppointmentSource.COACH_PLAN);
        existing.setSourceRef("coach-2026-10-19");

        when(repo.findById(5L)).thenReturn(Optional.of(existing));
        when(repo.findByApptDateAndUser_Id(any(), any())).thenReturn(List.of());
        when(repo.save(any(appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        appointmentDTO dto = new appointmentDTO(5L, "Course perso", null, null,
                LocalDate.of(2026, 10, 19), null, null, appointmentType.SPORT, null);

        appointmentDTO result = service.update(5L, USER_ID, dto);

        assertEquals(AppointmentSource.MANUAL, existing.getSource());
        assertNull(existing.getSourceRef());
        assertEquals("Course perso", result.getTitle());
    }

    @Test
    void update_notOwner_throwsNotFound() {
        appointment existing = new appointment();
        existing.setId(5L);
        User owner = mock(User.class);
        when(owner.getId()).thenReturn(2L);
        existing.setUser(owner);

        when(repo.findById(5L)).thenReturn(Optional.of(existing));

        appointmentDTO dto = new appointmentDTO(5L, "x", null, null, LocalDate.now(), null, null, appointmentType.AUTRE, null);

        assertThrows(ResponseStatusException.class, () -> service.update(5L, USER_ID, dto));
    }

    @Test
    void deleteBySourceForWeek_deletesOnlyMatchingEntries() {
        appointment a = new appointment();
        appointment b = new appointment();
        when(repo.findByUser_IdAndSourceAndApptDateBetween(
                USER_ID, AppointmentSource.COACH_PLAN, LocalDate.of(2026, 10, 19), LocalDate.of(2026, 10, 25)))
                .thenReturn(List.of(a, b));

        service.deleteBySourceForWeek(USER_ID, AppointmentSource.COACH_PLAN,
                LocalDate.of(2026, 10, 19), LocalDate.of(2026, 10, 25));

        verify(repo).deleteAll(List.of(a, b));
    }
}
