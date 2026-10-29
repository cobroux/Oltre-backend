package io.oltre_backend.calendar;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.oltre_backend.appointment.appointmentRepository;
import io.oltre_backend.auth.CurrentUser;
import io.oltre_backend.user.User;
import io.oltre_backend.user.UserRepository;

// Abonnement agenda en lecture seule (Oltre -> Google/Apple/Outlook) :
// l'utilisateur génère un jeton secret, l'URL /feed/<jeton>.ics est publique.
@RestController
@RequestMapping("/api/calendar")
public class CalendarFeedController {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final appointmentRepository appointmentRepository;

    public CalendarFeedController(UserRepository userRepository, appointmentRepository appointmentRepository) {
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping("/token")
    public Map<String, String> getToken() {
        return Collections.singletonMap("token", currentUser().getCalendarToken());
    }

    // Crée ou remplace le jeton : l'ancienne URL cesse aussitôt de fonctionner.
    @PostMapping("/token")
    @Transactional
    public Map<String, String> regenerateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        User user = currentUser();
        user.setCalendarToken(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
        return Map.of("token", userRepository.save(user).getCalendarToken());
    }

    @DeleteMapping("/token")
    @Transactional
    public ResponseEntity<Void> disable() {
        User user = currentUser();
        user.setCalendarToken(null);
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/feed/{token}.ics")
    public ResponseEntity<String> feed(@PathVariable String token) {
        User user = userRepository.findByCalendarToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        // Un an d'historique suffit à l'agenda et borne la taille du flux.
        var appointments = appointmentRepository.findByUser_IdAndApptDateGreaterThanEqual(
                user.getId(), LocalDate.now().minusYears(1));
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "calendar", StandardCharsets.UTF_8))
                .body(IcsWriter.write(appointments, Instant.now()));
    }

    private User currentUser() {
        return userRepository.findById(CurrentUser.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }
}
