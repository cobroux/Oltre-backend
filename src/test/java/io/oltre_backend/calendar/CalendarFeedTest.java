package io.oltre_backend.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import io.oltre_backend.AbstractIntegrationTest;
import io.oltre_backend.appointment.appointment;
import io.oltre_backend.appointment.appointmentRepository;
import io.oltre_backend.auth.JwtService;
import io.oltre_backend.user.User;
import io.oltre_backend.user.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "app.jwt.secret=test-secret-at-least-32-bytes-long-0123456789")
class CalendarFeedTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private appointmentRepository appointmentRepository;

    private final HttpClient http = HttpClient.newHttpClient();

    private HttpResponse<String> send(String method, String path, String jwt) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .method(method, HttpRequest.BodyPublishers.noBody());
        if (jwt != null) builder.header("Authorization", "Bearer " + jwt);
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String token(HttpResponse<String> response) {
        return response.body().replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
    }

    @Test
    void feed_isPublicWithToken_andRegenerationRevokesOldUrl() throws Exception {
        User user = userRepository.save(new User("IcsUser", 30, LocalDate.of(1996, 1, 1)));
        appointment a = new appointment();
        a.setTitle("Kiné");
        a.setApptDate(LocalDate.now().plusDays(2));
        a.setApptTime(LocalTime.of(14, 0));
        a.setUser(user);
        appointmentRepository.save(a);
        String jwt = jwtService.generateToken(user.getId(), "ics@test.local");

        String first = token(send("POST", "/api/calendar/token", jwt));
        HttpResponse<String> feed = send("GET", "/api/calendar/feed/" + first + ".ics", null);
        assertEquals(200, feed.statusCode());
        assertTrue(feed.headers().firstValue("Content-Type").orElse("").startsWith("text/calendar"));
        assertTrue(feed.body().contains("SUMMARY:Kiné"), feed.body());

        String second = token(send("POST", "/api/calendar/token", jwt));
        assertNotEquals(first, second);
        assertEquals(404, send("GET", "/api/calendar/feed/" + first + ".ics", null).statusCode());
        assertEquals(200, send("GET", "/api/calendar/feed/" + second + ".ics", null).statusCode());

        assertEquals(204, send("DELETE", "/api/calendar/token", jwt).statusCode());
        assertEquals(404, send("GET", "/api/calendar/feed/" + second + ".ics", null).statusCode());
    }

    @Test
    void tokenEndpoints_requireAuth() throws Exception {
        assertEquals(401, send("POST", "/api/calendar/token", null).statusCode());
        assertEquals(404, send("GET", "/api/calendar/feed/inconnu.ics", null).statusCode());
    }
}
