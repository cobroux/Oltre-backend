package io.oltre_backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import io.oltre_backend.AbstractIntegrationTest;
import io.oltre_backend.user.UserRepository;
import io.oltre_backend.user.UserService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"app.jwt.secret=test-secret-at-least-32-bytes-long-0123456789",
        "app.registration.enabled=true", "spring.mail.host=localhost",
        "app.base-url=https://oltre.test"})
class PasswordResetTest extends AbstractIntegrationTest {

    private static final Pattern LINK = Pattern.compile("https://oltre\\.test/reset-password\\?token=([A-Za-z0-9_-]+)");

    @LocalServerPort private int port;
    @Autowired private UserRepository userRepository;
    @Autowired private UserService userService;
    @Autowired private JdbcTemplate jdbc;
    @MockitoBean private JavaMailSender mailSender;

    private final HttpClient http = HttpClient.newHttpClient();
    private String email;
    // Chaque test a sa propre IP (X-Forwarded-For) pour ne pas partager la limite de débit.
    private String ip;

    @BeforeEach
    void setUp() throws Exception {
        email = "reset-" + UUID.randomUUID() + "@test.local";
        ip = "10." + (int) (Math.random() * 250) + "." + (int) (Math.random() * 250) + "." + (int) (Math.random() * 250);
        reset(mailSender);
        assertEquals(201, post("/auth/register", "{\"username\":\"rst\",\"email\":\"" + email
                + "\",\"password\":\"oldpassword1\",\"acceptTerms\":true}").statusCode());
    }

    @AfterEach
    void tearDown() {
        userService.deleteAccount(userRepository.findByEmail(email).getId());
    }

    private HttpResponse<String> post(String path, String json) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json").header("X-Forwarded-For", ip)
                .POST(HttpRequest.BodyPublishers.ofString(json)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private int forgot(String mail) throws Exception {
        return post("/auth/password/forgot", "{\"email\":\"" + mail + "\"}").statusCode();
    }

    private int resetPassword(String token, String password) throws Exception {
        return post("/auth/password/reset", "{\"token\":\"" + token + "\",\"password\":\"" + password + "\"}").statusCode();
    }

    private int login(String password) throws Exception {
        return post("/auth/login", "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}").statusCode();
    }

    /** Attend le n-ième email envoyé et en extrait le jeton du lien. */
    private String tokenFromMail(int nth) {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, timeout(5000).times(nth)).send(captor.capture());
        Matcher m = LINK.matcher(captor.getAllValues().get(nth - 1).getText());
        assertTrue(m.find());
        return m.group(1);
    }

    @Test
    void forgot_unknownEmail_returns204AndSendsNothing() throws Exception {
        assertEquals(204, forgot("nobody-" + UUID.randomUUID() + "@test.local"));
        assertEquals(204, forgot("pas-un-email"));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void forgot_knownEmail_sendsLink_andStoresOnlyTheHash() throws Exception {
        assertEquals(204, forgot(email));
        String token = tokenFromMail(1);

        List<String> hashes = jdbc.queryForList("select token_hash from password_reset_tokens", String.class);
        assertEquals(1, hashes.size());
        assertFalse(hashes.contains(token));
        assertEquals(64, hashes.get(0).length());
    }

    @Test
    void reset_validToken_changesPassword_thenTokenIsSingleUse() throws Exception {
        forgot(email);
        String token = tokenFromMail(1);

        assertEquals(204, resetPassword(token, "newpassword1"));
        assertEquals(200, login("newpassword1"));
        assertEquals(401, login("oldpassword1"));
        assertEquals(400, resetPassword(token, "another-password1"));
    }

    @Test
    void reset_expiredToken_returns400WithMessage() throws Exception {
        forgot(email);
        String token = tokenFromMail(1);
        jdbc.update("update password_reset_tokens set expires_at = now(6) - interval 1 minute");

        HttpResponse<String> r = post("/auth/password/reset", "{\"token\":\"" + token + "\",\"password\":\"newpassword1\"}");
        assertEquals(400, r.statusCode());
        assertTrue(r.body().contains("Ce lien n'est plus valable. Demande un nouveau lien."));
        assertEquals(200, login("oldpassword1"));
    }

    @Test
    void forgot_again_invalidatesThePreviousToken() throws Exception {
        forgot(email);
        String first = tokenFromMail(1);
        forgot(email);
        String second = tokenFromMail(2);

        assertEquals(400, resetPassword(first, "newpassword1"));
        assertEquals(204, resetPassword(second, "newpassword1"));
    }

    @Test
    void reset_weakPassword_returns400_andKeepsToken() throws Exception {
        forgot(email);
        String token = tokenFromMail(1);

        assertEquals(400, resetPassword(token, "short"));
        assertEquals(204, resetPassword(token, "newpassword1"));
    }

    @Test
    void forgot_rateLimited_stopsSendingAfterFiveRequests() throws Exception {
        for (int i = 0; i < 7; i++) assertEquals(204, forgot(email));
        verify(mailSender, timeout(5000).times(5)).send(any(SimpleMailMessage.class));
    }
}
