package io.oltre_backend.appointment;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.oltre_backend.AbstractIntegrationTest;
import jakarta.transaction.Transactional;

@Transactional
class AppointmentRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private appointmentRepository appointmentRepository;

    @Test
    void testSaveAppointment() {
        appointment appointment = new appointment();

        appointment.setTitle("Rendez-vous médecin");
        appointment.setDescription("Consultation annuelle");
        appointment.setLocation("Cabinet médical");
        appointment.setApptDate(LocalDate.of(2026, 8, 10));
        appointment.setApptTime(LocalTime.of(14, 30));
        appointment.setApptType(appointmentType.MEDICAL);

        appointment saved = appointmentRepository.save(appointment);

        assertNotNull(saved.getId());
        assertEquals("Rendez-vous médecin", saved.getTitle());
        assertEquals("Consultation annuelle", saved.getDescription());
        assertEquals("Cabinet médical", saved.getLocation());
        assertEquals(LocalDate.of(2026, 8, 10), saved.getApptDate());
        assertEquals(LocalTime.of(14, 30), saved.getApptTime());
        assertEquals(appointmentType.MEDICAL, saved.getApptType());
    }

    @Test
    void testFindAllAppointments() {
        appointment appointment = new appointment();

        appointment.setTitle("Dentiste");
        appointment.setDescription("Contrôle annuel");
        appointment.setLocation("Cabinet dentaire");
        appointment.setApptDate(LocalDate.now());
        appointment.setApptTime(LocalTime.of(10, 0));
        appointment.setApptType(appointmentType.MEDICAL);

        appointmentRepository.save(appointment);

        List<appointment> appointments = appointmentRepository.findAll();

        assertFalse(appointments.isEmpty());
        assertEquals("Dentiste", appointments.get(0).getTitle());
    }

    @Test
    void testFindAppointmentById() {
        appointment appointment = new appointment();

        appointment.setTitle("Réunion");
        appointment.setDescription("Réunion avec l'équipe");
        appointment.setApptDate(LocalDate.now());
        appointment.setApptTime(LocalTime.of(9, 30));
        appointment.setApptType(appointmentType.TRAVAIL);

        appointment saved = appointmentRepository.save(appointment);

        appointment found = appointmentRepository
                .findById(saved.getId())
                .orElse(null);

        assertNotNull(found);
        assertEquals("Réunion", found.getTitle());
        assertEquals("Réunion avec l'équipe", found.getDescription());
    }

    @Test
    void testDeleteAppointment() {
        appointment appointment = new appointment();

        appointment.setTitle("Rendez-vous à supprimer");
        appointment.setApptDate(LocalDate.now());
        appointment.setApptTime(LocalTime.of(16, 0));
        appointment.setApptType(appointmentType.TRAVAIL);

        appointment saved = appointmentRepository.save(appointment);

        appointmentRepository.delete(saved);

        assertFalse(
            appointmentRepository.findById(saved.getId()).isPresent()
        );
    }
}