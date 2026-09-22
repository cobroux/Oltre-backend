package io.oltre_backend.sport;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/garmin")
@CrossOrigin(origins = "http://localhost:4200")
public class GarminController {

    private final GarminService garminService;

    public GarminController(GarminService garminService) {
        this.garminService = garminService;
    }

    @PostMapping("/{userId}/connect")
    public ResponseEntity<Void> connect(@PathVariable Long userId, @RequestBody GarminConnectRequest request) {
        garminService.connect(userId, request.getEmail(), request.getPassword());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/status")
    public Map<String, Boolean> status(@PathVariable Long userId) {
        return Map.of("connected", garminService.isConnected(userId));
    }

    @DeleteMapping("/{userId}/connect")
    public ResponseEntity<Void> disconnect(@PathVariable Long userId) {
        garminService.disconnect(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/activities")
    public List<GarminActivityDTO> getActivities(
            @PathVariable Long userId,
            @RequestParam(required = false) String monday,
            @RequestParam(defaultValue = "50") int limit) {
        return garminService.getActivities(userId, monday, limit);
    }

    @ExceptionHandler(GarminAuthException.class)
    public ResponseEntity<Map<String, String>> handleAuthError(GarminAuthException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
    }
}
