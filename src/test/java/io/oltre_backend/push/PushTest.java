package io.oltre_backend.push;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import io.oltre_backend.AbstractIntegrationTest;
import io.oltre_backend.appointment.appointment;
import io.oltre_backend.appointment.appointmentRepository;
import io.oltre_backend.auth.JwtService;
import io.oltre_backend.coach.CoachPlan;
import io.oltre_backend.coach.CoachPlanRepository;
import io.oltre_backend.tasks.Tasks;
import io.oltre_backend.tasks.TasksRepository;
import io.oltre_backend.tasks.task_enum.TasksStatus;
import io.oltre_backend.user.User;
import io.oltre_backend.user.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "app.jwt.secret=test-secret-at-least-32-bytes-long-0123456789")
class PushTest extends AbstractIntegrationTest {

    // Lundi 2 novembre 2026
    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 2);

    @LocalServerPort private int port;
    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;
    @Autowired private PushSubscriptionRepository subscriptions;
    @Autowired private appointmentRepository appointmentRepository;
    @Autowired private TasksRepository tasksRepository;
    @Autowired private CoachPlanRepository coachPlanRepository;
    @Autowired private PushService pushService;
    @MockitoBean private PushSender sender;

    private final HttpClient http = HttpClient.newHttpClient();

    @BeforeEach
    void resetMock() {
        reset(sender);
    }

    private User newUser() {
        return userRepository.save(new User("Push" + System.nanoTime(), 30, LocalDate.of(1996, 1, 1)));
    }

    private void subscribe(User u, String endpoint) {
        pushService.subscribe(u.getId(), endpoint, "p256dh", "auth");
    }

    private appointment appt(User u, LocalDateTime start, String title) {
        appointment a = new appointment();
        a.setTitle(title);
        a.setApptDate(start.toLocalDate());
        a.setApptTime(start.toLocalTime());
        a.setUser(u);
        return appointmentRepository.save(a);
    }

    private List<PushService.Reminder> due(User u, LocalDateTime now) {
        return pushService.dueReminders(now).stream().filter(r -> r.userId().equals(u.getId())).toList();
    }

    // ── Calcul des rappels dus ───────────────────────────

    @Test
    void appointment_dueOnlyInsideWindow_andRespectsPreference() {
        User u = newUser();
        subscribe(u, "https://push.test/" + u.getId());
        LocalDateTime now = MONDAY.atTime(7, 0); // avant 8 h : pas de rappel tâches/coach
        appt(u, now.plusMinutes(30), "Dentiste");
        appt(u, now.plusMinutes(90), "Trop loin");
        appt(u, now.minusMinutes(5), "Passé");

        var due = due(u, now);
        assertEquals(1, due.size());
        assertTrue(due.get(0).body().contains("Rdv dans 30 min : Dentiste"), due.get(0).body());

        u.setPushApptMinutes(120);
        userRepository.save(u);
        assertEquals(2, due(u, now).size());

        u.setPushApptEnabled(false);
        userRepository.save(u);
        assertEquals(0, due(u, now).size());
    }

    @Test
    void movedAppointment_getsNewDedupKey() {
        User u = newUser();
        subscribe(u, "https://push.test/" + u.getId());
        LocalDateTime now = MONDAY.atTime(7, 0);
        appointment a = appt(u, now.plusMinutes(30), "Kiné");
        String before = due(u, now).get(0).dedupKey();
        a.setApptTime(now.plusMinutes(20).toLocalTime());
        appointmentRepository.save(a);
        assertTrue(!before.equals(due(u, now).get(0).dedupKey()));
    }

    @Test
    void tasks_dueOnlyAfterConfiguredTime_andIgnoreDone() {
        User u = newUser();
        subscribe(u, "https://push.test/" + u.getId());
        tasksRepository.save(task(u, "Payer loyer", TasksStatus.PENDING, MONDAY.minusDays(1))); // en retard
        tasksRepository.save(task(u, "Courses", TasksStatus.PENDING, MONDAY));
        tasksRepository.save(task(u, "Fait", TasksStatus.DONE, MONDAY));
        tasksRepository.save(task(u, "Demain", TasksStatus.PENDING, MONDAY.plusDays(1)));

        assertEquals(0, due(u, MONDAY.atTime(7, 59)).size());
        var due = due(u, MONDAY.atTime(8, 0));
        assertEquals(1, due.size());
        assertEquals("2 tâches aujourd'hui", due.get(0).title());
        assertEquals("tasks:" + MONDAY, due.get(0).dedupKey());
    }

    @Test
    void coach_dueOnSessionDay_notOnRestDay() {
        User u = newUser();
        subscribe(u, "https://push.test/" + u.getId());
        CoachPlan plan = new CoachPlan();
        plan.setUser(u);
        plan.setGeneratedAt(LocalDateTime.now());
        plan.setAnalysis("ok");
        plan.setDaysJson("[{\"day\":\"Lundi\",\"type\":\"Run\",\"label\":\"Footing 8 km\",\"distanceKm\":8.0,\"done\":false},"
                + "{\"day\":\"Mardi\",\"type\":\"Rest\",\"label\":\"Repos\",\"distanceKm\":null,\"done\":false}]");
        coachPlanRepository.save(plan);

        var monday = due(u, MONDAY.atTime(8, 0));
        assertEquals(1, monday.size());
        assertEquals("Séance du jour : Footing 8 km", monday.get(0).body());
        assertEquals(0, due(u, MONDAY.plusDays(1).atTime(8, 0)).size());
    }

    @Test
    void nothingDue_withoutSubscription() {
        User u = newUser();
        appt(u, MONDAY.atTime(7, 30), "Sans abonnement");
        assertEquals(0, due(u, MONDAY.atTime(7, 0)).size());
    }

    // ── Idempotence et nettoyage ─────────────────────────

    @Test
    void twoPasses_sendOnlyOnce() {
        User u = newUser();
        subscribe(u, "https://push.test/" + u.getId());
        when(sender.send(any(), any())).thenReturn(201);
        LocalDateTime now = MONDAY.atTime(7, 0);
        appt(u, now.plusMinutes(30), "Dentiste");

        pushService.sendDue(now);
        pushService.sendDue(now.plusMinutes(1));

        verify(sender, times(1)).send(any(), any());
    }

    @Test
    void gone410_deletesSubscription() {
        User u = newUser();
        subscribe(u, "https://push.test/gone/" + u.getId());
        when(sender.send(any(), any())).thenReturn(410);
        LocalDateTime now = MONDAY.atTime(7, 0);
        appt(u, now.plusMinutes(30), "Dentiste");

        pushService.sendDue(now);

        assertEquals(0, subscriptions.countByUserId(u.getId()));
    }

    // ── Endpoints ────────────────────────────────────────

    private HttpResponse<String> call(String method, String path, String jwt, String body) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        if (jwt != null) b.header("Authorization", "Bearer " + jwt);
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String sub(String endpoint) {
        return "{\"endpoint\":\"" + endpoint + "\",\"expirationTime\":null,\"keys\":{\"p256dh\":\"k\",\"auth\":\"a\"}}";
    }

    @Test
    void subscribe_upsertReassigns_andUnsubscribeIsolatedPerUser() throws Exception {
        User a = newUser();
        User b = newUser();
        String ja = jwtService.generateToken(a.getId(), "a@test.local");
        String jb = jwtService.generateToken(b.getId(), "b@test.local");
        String endpoint = "https://push.test/dev/" + a.getId();
        String q = "/api/push/subscriptions?endpoint=" + URLEncoder.encode(endpoint, StandardCharsets.UTF_8);

        assertEquals(204, call("POST", "/api/push/subscriptions", ja, sub(endpoint)).statusCode());
        assertEquals(204, call("POST", "/api/push/subscriptions", ja, sub(endpoint)).statusCode());
        assertEquals(1, subscriptions.countByUserId(a.getId()));

        assertEquals(204, call("DELETE", q, jb, null).statusCode()); // pas le sien : sans effet
        assertEquals(1, subscriptions.countByUserId(a.getId()));

        assertEquals(204, call("POST", "/api/push/subscriptions", jb, sub(endpoint)).statusCode()); // réattribué
        assertEquals(0, subscriptions.countByUserId(a.getId()));
        assertEquals(1, subscriptions.countByUserId(b.getId()));

        assertEquals(204, call("DELETE", q, jb, null).statusCode());
        assertEquals(0, subscriptions.countByUserId(b.getId()));
        assertEquals(204, call("DELETE", q, jb, null).statusCode()); // inexistant : 204 aussi
        assertEquals(401, call("POST", "/api/push/subscriptions", null, sub(endpoint)).statusCode());
        assertEquals(400, call("POST", "/api/push/subscriptions", ja, "{\"endpoint\":\"x\"}").statusCode());
    }

    @Test
    void preferences_defaults_put_andValidation() throws Exception {
        User u = newUser();
        String jwt = jwtService.generateToken(u.getId(), "p@test.local");

        HttpResponse<String> get = call("GET", "/api/push/preferences", jwt, null);
        assertEquals(200, get.statusCode());
        assertEquals("{\"apptEnabled\":true,\"apptMinutesBefore\":60,\"tasksEnabled\":true,\"tasksTime\":\"08:00\",\"coachEnabled\":true,\"subscriptionCount\":0}", get.body());

        String ok = "{\"apptEnabled\":false,\"apptMinutesBefore\":15,\"tasksEnabled\":true,\"tasksTime\":\"18:30\",\"coachEnabled\":false}";
        HttpResponse<String> put = call("PUT", "/api/push/preferences", jwt, ok);
        assertEquals(200, put.statusCode());
        assertTrue(put.body().contains("\"tasksTime\":\"18:30\"") && put.body().contains("\"apptMinutesBefore\":15"), put.body());
        assertEquals(LocalTime.of(18, 30), userRepository.findById(u.getId()).orElseThrow().getPushTasksTime());

        assertEquals(400, call("PUT", "/api/push/preferences", jwt, ok.replace("15", "4")).statusCode());
        assertEquals(400, call("PUT", "/api/push/preferences", jwt, ok.replace("15", "1441")).statusCode());
        assertEquals(400, call("PUT", "/api/push/preferences", jwt, ok.replace("18:30", "25:00")).statusCode());
        assertEquals(400, call("PUT", "/api/push/preferences", jwt, ok.replace("18:30", "8h")).statusCode());
    }

    @Test
    void publicKey_503WhenNotConfigured_200WhenConfigured() throws Exception {
        User u = newUser();
        String jwt = jwtService.generateToken(u.getId(), "k@test.local");
        assertEquals(503, call("GET", "/api/push/public-key", jwt, null).statusCode());

        when(sender.isConfigured()).thenReturn(true);
        when(sender.publicKey()).thenReturn("BPublicKey");
        HttpResponse<String> ok = call("GET", "/api/push/public-key", jwt, null);
        assertEquals(200, ok.statusCode());
        assertEquals("{\"publicKey\":\"BPublicKey\"}", ok.body());
    }

    @Test
    void test_sendsToAllDevicesOfUser() throws Exception {
        User u = newUser();
        subscribe(u, "https://push.test/t1/" + u.getId());
        subscribe(u, "https://push.test/t2/" + u.getId());
        when(sender.isConfigured()).thenReturn(true);
        when(sender.send(any(), any())).thenReturn(201);
        String jwt = jwtService.generateToken(u.getId(), "t@test.local");

        assertEquals(204, call("POST", "/api/push/test", jwt, null).statusCode());
        verify(sender, times(2)).send(any(), any());
    }

    private static Tasks task(User u, String name, TasksStatus status, LocalDate date) {
        Tasks t = new Tasks();
        t.setTasksName(name);
        t.setTasksStatus(status);
        t.setTasksDate(date);
        t.setUser(u);
        return t;
    }
}
