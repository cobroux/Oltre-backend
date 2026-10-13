package io.oltre_backend.sport;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.oltre_backend.auth.CurrentUser;

@RestController
@RequestMapping("/api/garmin")
// Pas de @CrossOrigin ici : l'annotation écrasait la politique CORS globale
// (SecurityConfig, basée sur ALLOWED_ORIGINS) avec un localhost:4200 figé en
// dur, ce qui aurait bloqué le vrai frontend en production tout en gardant
// un accès cross-origin ouvert depuis n'importe quel build local du
// frontend. Hérite maintenant de la même politique que le reste de l'API.
public class GarminController {

    private final GarminService garminService;
    private final GarminCalendarSyncService calendarSyncService;

    public GarminController(GarminService garminService, GarminCalendarSyncService calendarSyncService) {
        this.garminService = garminService;
        this.calendarSyncService = calendarSyncService;
    }

    @PostMapping("/connect")
    public ResponseEntity<Void> connect(@RequestBody GarminConnectRequest request) {
        garminService.connect(CurrentUser.id(), request.getEmail(), request.getPassword());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public Map<String, Boolean> status() {
        return Map.of("connected", garminService.isConnected(CurrentUser.id()));
    }

    @DeleteMapping("/connect")
    public ResponseEntity<Void> disconnect() {
        garminService.disconnect(CurrentUser.id());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/activities")
    public List<GarminActivityDTO> getActivities(
            @RequestParam(required = false) String monday,
            @RequestParam(defaultValue = "50") int limit) {
        List<GarminActivityDTO> activities = garminService.getActivities(CurrentUser.id(), monday, limit);
        // Remplit le calendrier au passage (idempotent, voir
        // GarminCalendarSyncService) - pas de synchro séparée à déclencher,
        // ça se fait à chaque fois que la page Sport charge des activités.
        calendarSyncService.sync(CurrentUser.id(), activities);
        return activities;
    }

    @GetMapping("/records")
    public GarminRecordsResponseDTO getRecords() {
        return garminService.getRecords(CurrentUser.id());
    }

    @GetMapping("/activities/{activityId}/details")
    public GarminActivityDetailDTO getActivityDetails(@PathVariable String activityId) {
        return garminService.getActivityDetails(CurrentUser.id(), activityId);
    }

    @ExceptionHandler(GarminAuthException.class)
    public ResponseEntity<Map<String, String>> handleAuthError(GarminAuthException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
    }
}
