package io.oltre_backend.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import io.oltre_backend.AbstractIntegrationTest;
import io.oltre_backend.tasks.Tasks;
import io.oltre_backend.tasks.TasksRepository;
import io.oltre_backend.user.User;
import io.oltre_backend.user.UserRepository;

// Régression : toutes les erreurs ressortaient en 403 car /error exigeait un
// jeton, et un jeton absent ou expiré donnait aussi 403 au lieu de 401 (le
// frontend ne déconnectait donc jamais l'utilisateur).
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "app.jwt.secret=test-secret-at-least-32-bytes-long-0123456789")
class ApiErrorStatusTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TasksRepository tasksRepository;

    private final HttpClient http = HttpClient.newHttpClient();

    private HttpResponse<String> send(String method, String path, String token, String json) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .method(method, json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private Long createUser() {
        return userRepository.save(new User("ErrUser", 30, LocalDate.of(1996, 1, 1))).getId();
    }

    @Test
    void missingToken_returns401() throws Exception {
        assertEquals(401, send("GET", "/api/tasks/all", null, null).statusCode());
    }

    @Test
    void invalidToken_returns401() throws Exception {
        assertEquals(401, send("GET", "/api/tasks/all", "not-a-jwt", null).statusCode());
    }

    @Test
    void unknownResource_returns404WithMessageBody() throws Exception {
        String token = jwtService.generateToken(createUser(), "err@test.local");

        HttpResponse<String> response = send("GET", "/api/tasks/999999", token, null);

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("\"message\""), response.body());
    }

    @Test
    void malformedJson_returns400() throws Exception {
        String token = jwtService.generateToken(createUser(), "err@test.local");

        assertEquals(400, send("POST", "/api/tasks/save", token, "{malformed").statusCode());
    }

    @Test
    void invalidTaskStatus_returns400() throws Exception {
        Long userId = createUser();
        Tasks task = new Tasks();
        task.setTasksName("Tâche");
        task.setUser(userRepository.getReferenceById(userId));
        Long taskId = tasksRepository.save(task).getId();
        String token = jwtService.generateToken(userId, "err@test.local");

        HttpResponse<String> response = send("PUT", "/api/tasks/update", token,
                "{\"id\":\"" + taskId + "\",\"status\":\"NOPE\"}");

        assertEquals(400, response.statusCode());
    }

    @Test
    void loginWithoutPassword_returns401() throws Exception {
        assertEquals(401, send("POST", "/auth/login", null, "{\"email\":\"x@test.local\"}").statusCode());
    }

    @Test
    void taskUpdateWithoutId_returns400() throws Exception {
        String token = jwtService.generateToken(createUser(), "err@test.local");

        assertEquals(400, send("PUT", "/api/tasks/update", token, "{\"status\":\"DONE\"}").statusCode());
        assertEquals(400, send("PUT", "/api/tasks/update", token, "{\"id\":\"abc\",\"status\":\"DONE\"}").statusCode());
    }

    @Test
    void expenseWithoutRequiredFields_returns400WithMessage() throws Exception {
        String token = jwtService.generateToken(createUser(), "err@test.local");

        HttpResponse<String> response = send("POST", "/api/expenses/save", token, "{\"expensesName\":\"x\"}");

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("\"message\""), response.body());
    }

    @Test
    void expenseEndBeforeStart_returns400() throws Exception {
        String token = jwtService.generateToken(createUser(), "err@test.local");

        assertEquals(400, send("POST", "/api/expenses/save", token,
                "{\"expensesName\":\"x\",\"amount\":5,\"recType\":\"Monthly\","
                + "\"startDate\":\"2026-05-10\",\"endDate\":\"2026-05-01\"}").statusCode());
    }

    @Test
    void validExpense_returns200() throws Exception {
        String token = jwtService.generateToken(createUser(), "err@test.local");

        assertEquals(200, send("POST", "/api/expenses/save", token,
                "{\"expensesName\":\"x\",\"amount\":5,\"recType\":\"Monthly\",\"startDate\":\"2026-01-31\"}").statusCode());
    }

    @Test
    void appointmentWithoutTitle_returns400() throws Exception {
        String token = jwtService.generateToken(createUser(), "err@test.local");

        assertEquals(400, send("POST", "/api/appointments/save", token, "{\"apptDate\":\"2026-10-19\"}").statusCode());
    }

    @Test
    void goalWithoutName_returns400() throws Exception {
        String token = jwtService.generateToken(createUser(), "err@test.local");

        assertEquals(400, send("POST", "/api/coach/goals", token, "{\"goalType\":\"RACE\"}").statusCode());
    }

    @Test
    void mealWithoutName_returns400() throws Exception {
        String token = jwtService.generateToken(createUser(), "err@test.local");

        assertEquals(400, send("POST", "/api/meals/save", token, "{\"calories\":100}").statusCode());
    }

    @Test
    void registerWithTooLongUsername_returns400() throws Exception {
        String longName = "a".repeat(300);

        assertEquals(400, send("POST", "/auth/register", null,
                "{\"username\":\"" + longName + "\",\"email\":\"a@b.co\",\"password\":\"password123\"}").statusCode());
    }
}
