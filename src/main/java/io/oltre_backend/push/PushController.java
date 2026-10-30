package io.oltre_backend.push;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.oltre_backend.auth.CurrentUser;
import io.oltre_backend.user.User;
import io.oltre_backend.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@RestController
@RequestMapping("/api/push")
public class PushController {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private final PushService pushService;
    private final UserRepository userRepository;

    public PushController(PushService pushService, UserRepository userRepository) {
        this.pushService = pushService;
        this.userRepository = userRepository;
    }

    @GetMapping("/public-key")
    public Map<String, String> publicKey() {
        if (!pushService.isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Notifications push non configurées");
        }
        return Map.of("publicKey", pushService.publicKey());
    }

    @PostMapping("/subscriptions")
    public ResponseEntity<Void> subscribe(@Valid @RequestBody SubscriptionRequest request) {
        pushService.subscribe(CurrentUser.id(), request.endpoint(), request.keys().p256dh(), request.keys().auth());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/subscriptions")
    public ResponseEntity<Void> unsubscribe(@RequestParam String endpoint) {
        pushService.unsubscribe(CurrentUser.id(), endpoint);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/preferences")
    public PreferencesResponse getPreferences() {
        return toResponse(currentUser());
    }

    @PutMapping("/preferences")
    public PreferencesResponse updatePreferences(@Valid @RequestBody PreferencesRequest request) {
        User user = currentUser();
        user.setPushApptEnabled(request.apptEnabled());
        user.setPushApptMinutes(request.apptMinutesBefore());
        user.setPushTasksEnabled(request.tasksEnabled());
        user.setPushTasksTime(LocalTime.parse(request.tasksTime(), HH_MM));
        user.setPushCoachEnabled(request.coachEnabled());
        return toResponse(userRepository.save(user));
    }

    @PostMapping("/test")
    public ResponseEntity<Void> test() {
        pushService.sendTest(CurrentUser.id());
        return ResponseEntity.noContent().build();
    }

    private User currentUser() {
        return userRepository.findById(CurrentUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private PreferencesResponse toResponse(User u) {
        return new PreferencesResponse(u.isPushApptEnabled(), u.getPushApptMinutes(), u.isPushTasksEnabled(),
                u.getPushTasksTime().format(HH_MM), u.isPushCoachEnabled(), pushService.subscriptionCount(u.getId()));
    }

    // expirationTime et autres champs de PushSubscription.toJSON() sont ignorés (Jackson ne les connaît pas).
    record SubscriptionRequest(@NotBlank @jakarta.validation.constraints.Size(max = 512) String endpoint, @NotNull @Valid Keys keys) {
        record Keys(@NotBlank String p256dh, @NotBlank String auth) {}
    }

    record PreferencesRequest(boolean apptEnabled,
                              @Min(5) @Max(1440) int apptMinutesBefore,
                              boolean tasksEnabled,
                              @NotNull @Pattern(regexp = "([01]\\d|2[0-3]):[0-5]\\d", message = "Heure au format HH:mm") String tasksTime,
                              boolean coachEnabled) {}

    record PreferencesResponse(boolean apptEnabled, int apptMinutesBefore, boolean tasksEnabled, String tasksTime,
                               boolean coachEnabled, long subscriptionCount) {}
}
