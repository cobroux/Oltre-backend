package io.oltre_backend.appointment;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
public class appointmentController {
    private final appointmentService service;
    public appointmentController(appointmentService service) { this.service = service; }

    @GetMapping("/week")
    public List<appointmentDTO> getWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate monday) {
        return service.getWeek(monday);
    }

    @PostMapping("/save")
    public appointmentDTO save(@RequestBody appointmentDTO dto) { return service.save(dto); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }
}
