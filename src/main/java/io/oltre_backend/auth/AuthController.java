package io.oltre_backend.auth;

import io.oltre_backend.user.User;
import io.oltre_backend.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    // Hash BCrypt factice utilisé pour égaliser le temps de réponse de
    // /login quand l'email n'existe pas (voir le commentaire dans login()).
    // N'importe quel hash BCrypt valide convient - celui-ci ne correspond à
    // aucun mot de passe réel.
    private static final String DUMMY_PASSWORD_HASH =
            "$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5L5FAUyqnBAjQexwF.7a0vtlUiQ9q";

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final boolean registrationEnabled;
    private final PasswordResetService passwordResetService;

    public AuthController(UserRepository userRepository,
                          JwtService jwtService,
                          PasswordEncoder passwordEncoder,
                          LoginAttemptService loginAttemptService,
                          @Value("${app.registration.enabled:true}") boolean registrationEnabled,
                          PasswordResetService passwordResetService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptService = loginAttemptService;
        this.registrationEnabled = registrationEnabled;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest http) {
        String clientIp = http.getRemoteAddr();
        if (request.email() == null || request.password() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Email ou mot de passe incorrect"));
        }

        if (loginAttemptService.isLocked(request.email(), clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("message", "Trop de tentatives échouées. Réessaie dans quelques minutes."));
        }

        User user = userRepository.findByEmail(request.email());

        // Toujours passer par passwordEncoder.matches, même quand l'email
        // n'existe pas (contre un hash factice) : sinon la branche
        // user == null retourne quasi instantanément alors qu'un mauvais mot
        // de passe prend le temps du calcul BCrypt, ce qui permet de
        // deviner quels emails sont inscrits en mesurant le temps de
        // réponse.
        String hashToCheck = user != null ? user.getPassword() : DUMMY_PASSWORD_HASH;
        boolean passwordMatches = passwordEncoder.matches(request.password(), hashToCheck);

        if (user == null || !passwordMatches) {
            loginAttemptService.recordFailure(request.email(), clientIp);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Email ou mot de passe incorrect"));
        }
        loginAttemptService.recordSuccess(request.email(), clientIp);

        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", user.getId(),
                "username", user.getUsername()
        ));
    }

    @GetMapping("/registration")
    public Map<String, Boolean> registration() {
        return Map.of("enabled", registrationEnabled);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        // Appli perso : en prod, pas de comptes tiers (quota Gemini, appels Garmin, données).
        if (!registrationEnabled) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Les inscriptions sont fermées"));
        }
        if (!Boolean.TRUE.equals(request.acceptTerms())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tu dois accepter les CGU et la politique de confidentialité"));
        }
        if (request.username() == null || request.username().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le nom d'utilisateur est requis"));
        }
        if (request.email() == null || !request.email().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email invalide"));
        }
        if (request.password() == null || request.password().length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le mot de passe doit contenir au moins 8 caractères"));
        }
        if (userRepository.existsByEmail(request.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Cet email est déjà utilisé"));
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setAge(request.age());
        user.setBirthDate(request.birthDate());
        user.setTermsAcceptedAt(LocalDateTime.now());
        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "token", token,
                "userId", user.getId(),
                "username", user.getUsername()
        ));
    }

    // Toujours 204 : aucune différence de réponse entre email connu, inconnu ou limité.
    @PostMapping("/password/forgot")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotRequest request, HttpServletRequest http) {
        passwordResetService.requestReset(request.email(), http.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetRequest request, HttpServletRequest http) {
        if (request.password() == null || request.password().length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le mot de passe doit contenir au moins 8 caractères"));
        }
        if (!passwordResetService.reset(request.token(), request.password(), http.getRemoteAddr())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Ce lien n'est plus valable. Demande un nouveau lien."));
        }
        return ResponseEntity.noContent().build();
    }

    record ForgotRequest(String email) {}
    record ResetRequest(String token,
            @Size(max = 128, message = "Mot de passe trop long") String password) {}
    record LoginRequest(String email, String password) {}
    record RegisterRequest(
            @Size(max = 255, message = "Nom d'utilisateur trop long") String username,
            @Size(max = 255, message = "Email trop long") String email,
            @Size(max = 128, message = "Mot de passe trop long") String password,
            Integer age, LocalDate birthDate, Boolean acceptTerms) {}
}
