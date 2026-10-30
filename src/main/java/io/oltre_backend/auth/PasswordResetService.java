package io.oltre_backend.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.oltre_backend.user.User;
import io.oltre_backend.user.UserRepository;

@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int VALID_MINUTES = 30;

    private final SecureRandom random = new SecureRandom();
    private final PasswordResetTokenRepository tokens;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService limiter;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final String mailHost;
    private final String from;
    private final String baseUrl;

    public PasswordResetService(PasswordResetTokenRepository tokens, UserRepository users,
                                PasswordEncoder passwordEncoder, LoginAttemptService limiter,
                                ObjectProvider<JavaMailSender> mailSender,
                                @Value("${spring.mail.host:}") String mailHost,
                                @Value("${app.mail.from:}") String from,
                                @Value("${app.base-url}") String baseUrl) {
        this.tokens = tokens;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.limiter = limiter;
        this.mailSender = mailSender;
        this.mailHost = mailHost;
        this.from = from;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
    }

    // Ne révèle rien à l'appelant. Limite : 5 demandes / 15 min par IP et par email
    // (LoginAttemptService) ; au-delà on n'envoie plus, sans changer la réponse.
    public void requestReset(String email, String clientIp) {
        if (email == null || email.isBlank() || email.length() > 255) return;
        String emailKey = "forgot:" + email;
        if (limiter.isLocked("forgot", clientIp) || limiter.isLocked(emailKey, "*")) return;
        limiter.recordFailure("forgot", clientIp);
        limiter.recordFailure(emailKey, "*");

        User user = users.findByEmail(email);
        if (user == null) return;

        byte[] raw = new byte[32];
        random.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        tokens.deleteByUserId(user.getId());
        tokens.save(new PasswordResetToken(user.getId(), hash(token), LocalDateTime.now().plusMinutes(VALID_MINUTES)));

        JavaMailSender sender = mailSender.getIfAvailable();
        if (mailHost.isBlank() || sender == null) {
            log.warn("Réinitialisation de mot de passe demandée mais email non configuré (MAIL_HOST vide) : rien envoyé");
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        if (!from.isBlank()) msg.setFrom(from);
        msg.setTo(user.getEmail());
        msg.setSubject("Réinitialisation de ton mot de passe Oltre");
        msg.setText("Tu as demandé à réinitialiser ton mot de passe Oltre. Ce lien est valable "
                + VALID_MINUTES + " minutes : " + baseUrl + "/reset-password?token=" + token
                + "\n\nSi ce n'est pas toi, ignore cet email.");
        // Asynchrone et hors transaction : le temps SMTP ne doit pas distinguer un email connu d'un inconnu.
        CompletableFuture.runAsync(() -> {
            try {
                sender.send(msg);
            } catch (Exception e) {
                // Classe seule : le message d'une exception SMTP pourrait contenir le corps (donc le lien).
                log.warn("Échec d'envoi de l'email de réinitialisation : {}", e.getClass().getSimpleName());
            }
        });
    }

    /** @return false si le jeton est invalide, expiré ou déjà utilisé. */
    @Transactional
    public boolean reset(String token, String newPassword, String clientIp) {
        if (token == null) return false;
        String h = hash(token);
        Long userId = tokens.findUserId(h, LocalDateTime.now()).orElse(null);
        if (userId == null || tokens.consume(h, LocalDateTime.now()) != 1) return false;
        User user = users.findById(userId).orElseThrow();
        user.setPassword(passwordEncoder.encode(newPassword));
        users.save(user);
        // shortcut: ne débloque que l'IP de la requête ; les JWT déjà émis restent valides jusqu'à expiration (24 h).
        limiter.recordSuccess(user.getEmail(), clientIp);
        return true;
    }

    private static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
