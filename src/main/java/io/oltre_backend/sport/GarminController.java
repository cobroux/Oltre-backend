package io.oltre_backend.sport;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.oltre_backend.auth.CurrentUser;

@RestController
@RequestMapping("/api/garmin")
@CrossOrigin(origins = "http://localhost:4200")
public class GarminController {

    private final GarminService garminService;

    public GarminController(GarminService garminService) {
        this.garminService = garminService;
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
        return garminService.getActivities(CurrentUser.id(), monday, limit);
    }

    @ExceptionHandler(GarminAuthException.class)
    public ResponseEntity<Map<String, String>> handleAuthError(GarminAuthException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
    }
}
