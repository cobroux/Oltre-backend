package io.oltre_backend.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import io.oltre_backend.AbstractIntegrationTest;
import io.oltre_backend.appointment.AppointmentSource;
import io.oltre_backend.appointment.appointment;
import io.oltre_backend.appointment.appointmentRepository;
import io.oltre_backend.auth.JwtService;
import io.oltre_backend.coach.SportGoal;
import io.oltre_backend.coach.SportGoalRepository;
import io.oltre_backend.coach.SportGoalType;
import io.oltre_backend.expenses.Expenses;
import io.oltre_backend.expenses.ExpensesRepository;
import io.oltre_backend.expenses.RecType;
import io.oltre_backend.meal.Meal;
import io.oltre_backend.meal.MealRepository;
import io.oltre_backend.tasks.Tasks;
import io.oltre_backend.tasks.TasksRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "app.jwt.secret=test-secret-at-least-32-bytes-long-0123456789")
class UserExportTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired private UserService userService;
    private final List<Long> created = new ArrayList<>();
    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;
    @Autowired private appointmentRepository appointmentRepository;
    @Autowired private TasksRepository tasksRepository;
    @Autowired private MealRepository mealRepository;
    @Autowired private ExpensesRepository expensesRepository;
    @Autowired private SportGoalRepository sportGoalRepository;

    private final HttpClient http = HttpClient.newHttpClient();

    private HttpResponse<String> get(String jwt) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/users/me/export")).GET();
        if (jwt != null) b.header("Authorization", "Bearer " + jwt);
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private User seed(String tag, boolean garmin) {
        User u = new User("user-" + tag, 30, LocalDate.of(1996, 1, 1));
        u.setEmail(tag + "@export.test");
        u.setPassword("HASH-" + tag);
        u.setCalendarToken("ICSTOKEN-" + tag);
        u = userRepository.save(u);

        appointment a = new appointment();
        a.setTitle("rdv-" + tag);
        a.setApptDate(LocalDate.now());
        a.setSource(garmin ? AppointmentSource.GARMIN : AppointmentSource.MANUAL);
        a.setUser(u);
        appointmentRepository.save(a);

        Tasks t = new Tasks();
        t.setTasksName("tache-" + tag);
        t.setUser(u);
        tasksRepository.save(t);

        Meal m = new Meal();
        m.setMealName("repas-" + tag);
        m.setUser(u);
        mealRepository.save(m);

        Expenses e = new Expenses("depense-" + tag, BigDecimal.TEN, RecType.Monthly, LocalDate.now(), null);
        e.setUser(u);
        expensesRepository.save(e);

        SportGoal g = new SportGoal(SportGoalType.TenK, "objectif-" + tag, LocalDate.now().plusDays(30), null);
        g.setUser(u);
        sportGoalRepository.save(g);
        created.add(u.getId());
        return u;
    }

    // La base est partagée : MealRepositoryTest.findAll() casse si on laisse des repas.
    @AfterEach
    void cleanup() {
        created.forEach(userService::deleteAccount);
    }

    @Test
    void export_containsOnlyOwnData_andNoSecret() throws Exception {
        User one = seed("alpha", true);
        seed("beta", false);

        HttpResponse<String> r = get(jwtService.generateToken(one.getId(), "alpha@export.test"));

        assertEquals(200, r.statusCode());
        assertTrue(r.headers().firstValue("Content-Disposition").orElse("").contains("oltre-export.json"));
        String body = r.body();
        for (String key : new String[] {"exportedAt", "profile", "appointments", "tasks", "meals", "expenses",
                "sportGoals", "coachPlan", "garminActivities"}) {
            assertTrue(body.contains("\"" + key + "\""), key);
        }
        for (String own : new String[] {"alpha@export.test", "rdv-alpha", "tache-alpha", "repas-alpha", "depense-alpha", "objectif-alpha"}) {
            assertTrue(body.contains(own), own);
        }
        // rdv Garmin présent à la fois dans appointments et garminActivities
        assertEquals(2, body.split("rdv-alpha", -1).length - 1);
        for (String forbidden : new String[] {"beta", "HASH-", "ICSTOKEN", "password", "calendarToken"}) {
            assertFalse(body.contains(forbidden), forbidden);
        }
    }

    @Test
    void export_requiresAuth() throws Exception {
        assertEquals(401, get(null).statusCode());
    }
}
