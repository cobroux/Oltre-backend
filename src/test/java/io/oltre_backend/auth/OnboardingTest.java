package io.oltre_backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import io.oltre_backend.AbstractIntegrationTest;
import io.oltre_backend.user.UserRepository;
import io.oltre_backend.user.UserService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"app.jwt.secret=test-secret-at-least-32-bytes-long-0123456789",
        "app.registration.enabled=true"})
class OnboardingTest extends AbstractIntegrationTest {

    @LocalServerPort private int port;
    @Autowired private UserRepository userRepository;
    @Autowired private UserService userService;

    private final HttpClient http = HttpClient.newHttpClient();

    private HttpResponse<String> send(String method, String path, String token, String json) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .method(method, json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json));
        if (token != null) b.header("Authorization", "Bearer " + token);
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String register(String extra) throws Exception {
        return "{\"username\":\"onb\",\"email\":\"onb@test.local\",\"password\":\"password123\"" + extra + "}";
    }

    @Test
    void registrationStatus_isPublic() throws Exception {
        HttpResponse<String> r = send("GET", "/auth/registration", null, null);
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("\"enabled\":true"));
    }

    @Test
    void register_requiresTerms_thenOnboardingFlow() throws Exception {
        assertEquals(400, send("POST", "/auth/register", null, register("")).statusCode());
        assertEquals(400, send("POST", "/auth/register", null, register(",\"acceptTerms\":false")).statusCode());

        HttpResponse<String> r = send("POST", "/auth/register", null, register(",\"acceptTerms\":true"));
        assertEquals(201, r.statusCode());
        String token = r.body().replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
        Long id = userRepository.findByEmail("onb@test.local").getId();
        try {
            assertNotNull(userRepository.findById(id).orElseThrow().getTermsAcceptedAt());
            assertTrue(send("GET", "/api/users/me", token, null).body().contains("\"onboardingDone\":false"));

            assertEquals(204, send("POST", "/api/users/me/onboarding", token, null).statusCode());
            assertEquals(204, send("POST", "/api/users/me/onboarding", token, null).statusCode());
            assertTrue(send("GET", "/api/users/me", token, null).body().contains("\"onboardingDone\":true"));
        } finally {
            userService.deleteAccount(id);
        }
    }

    @Test
    void onboarding_requiresAuth() throws Exception {
        assertEquals(401, send("POST", "/api/users/me/onboarding", null, null).statusCode());
    }
}
