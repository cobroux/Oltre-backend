package io.oltre_backend.sport;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.oltre_backend.appointment.AppointmentSource;
import io.oltre_backend.appointment.appointmentService;

class GarminCalendarSyncServiceTest {

    private final appointmentService appointmentService = mock(appointmentService.class);
    private final GarminCalendarSyncService service = new GarminCalendarSyncService(appointmentService);

    private static final Long USER_ID = 1L;

    private GarminActivityDTO activity(Long id, String name, String sportTypeKey, String startLocal,
                                        Double distance, Double duration) {
        GarminActivity a = new GarminActivity();
        a.setId(id);
        a.setName(name);
        if (sportTypeKey != null) {
            GarminActivity.ActivityType type = new GarminActivity.ActivityType();
            type.setTypeKey(sportTypeKey);
            a.setActivityType(type);
        }
        a.setStartLocal(startLocal);
        a.setDistance(distance);
        a.setDuration(duration);
        return new GarminActivityDTO(a);
    }

    @Test
    void sync_upsertsEachActivity_keyedByGarminId() {
        GarminActivityDTO a = activity(42L, "Sortie matinale", "running", "2026-10-10T07:12:00", 8200.0, 2520.0);

        service.sync(USER_ID, List.of(a));

        verify(appointmentService).upsertFromSource(
                eq(USER_ID), eq("Sortie matinale"), eq("8.2 km · 42 min"),
                eq(LocalDate.of(2026, 10, 10)), eq(LocalTime.of(7, 12, 0)),
                eq(AppointmentSource.GARMIN), eq("garmin-42"));
    }

    @Test
    void sync_sameActivityTwice_sameSourceRef_noDuplicateKey() {
        GarminActivityDTO a = activity(42L, "Sortie matinale", "running", "2026-10-10T07:12:00", 8200.0, 2520.0);

        service.sync(USER_ID, List.of(a));
        service.sync(USER_ID, List.of(a));

        // upsertFromSource lui-même gère l'idempotence (voir
        // AppointmentServiceTest) - on vérifie ici que la même activité
        // produit systématiquement le même sourceRef, condition nécessaire à
        // la dédup.
        verify(appointmentService, org.mockito.Mockito.times(2))
                .upsertFromSource(any(), any(), any(), any(), any(), eq(AppointmentSource.GARMIN), eq("garmin-42"));
    }

    @Test
    void sync_noActivityName_fallsBackToSportTypeLabel() {
        GarminActivityDTO a = activity(7L, null, "cycling", "2026-10-11T09:00:00", 25000.0, 3600.0);

        service.sync(USER_ID, List.of(a));

        verify(appointmentService).upsertFromSource(
                eq(USER_ID), eq("Vélo"), any(), any(), any(), eq(AppointmentSource.GARMIN), eq("garmin-7"));
    }

    @Test
    void sync_unparseableDate_skipsActivity_withoutThrowing() {
        GarminActivityDTO a = activity(9L, "Bug", "running", "not-a-date", null, null);

        assertDoesNotThrow(() -> service.sync(USER_ID, List.of(a)));
        verify(appointmentService, never()).upsertFromSource(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void sync_activityWithoutId_isSkipped() {
        GarminActivityDTO a = activity(null, "Pas d'id", "running", "2026-10-10T07:12:00", 1000.0, 300.0);

        service.sync(USER_ID, List.of(a));

        verify(appointmentService, never()).upsertFromSource(any(), any(), any(), any(), any(), any(), any());
    }
}
