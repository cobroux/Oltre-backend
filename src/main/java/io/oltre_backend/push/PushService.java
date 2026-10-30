package io.oltre_backend.push;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.oltre_backend.appointment.appointmentRepository;
import io.oltre_backend.coach.CoachService;
import io.oltre_backend.coach.PlanDayDTO;
import io.oltre_backend.tasks.TasksRepository;
import io.oltre_backend.tasks.task_enum.TasksStatus;
import io.oltre_backend.user.User;
import io.oltre_backend.user.UserRepository;

@Service
public class PushService {

    private static final Logger log = LoggerFactory.getLogger(PushService.class);
    // shortcut: fuseau fixe Europe/Paris (comme le flux .ics), à rendre configurable par utilisateur si l'appli sort de France.
    static final ZoneId ZONE = ZoneId.of("Europe/Paris");
    private static final List<String> WEEK_DAYS = List.of("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Un rappel à envoyer ; dedupKey identifie l'événement (un rdv déplacé a une autre clé). */
    public record Reminder(Long userId, String dedupKey, String title, String body, String url) {}

    private final PushSubscriptionRepository subscriptions;
    private final UserRepository users;
    private final appointmentRepository appointments;
    private final TasksRepository tasks;
    private final CoachService coachService;
    private final PushSender sender;
    private final JdbcTemplate jdbc;

    public PushService(PushSubscriptionRepository subscriptions, UserRepository users, appointmentRepository appointments,
                       TasksRepository tasks, CoachService coachService, PushSender sender, JdbcTemplate jdbc) {
        this.subscriptions = subscriptions;
        this.users = users;
        this.appointments = appointments;
        this.tasks = tasks;
        this.coachService = coachService;
        this.sender = sender;
        this.jdbc = jdbc;
    }

    public boolean isConfigured() { return sender.isConfigured(); }

    public String publicKey() { return sender.publicKey(); }

    // ── Abonnements ──────────────────────────────────────

    @Transactional
    public void subscribe(Long userId, String endpoint, String p256dh, String auth) {
        PushSubscription sub = subscriptions.findByEndpoint(endpoint).orElseGet(PushSubscription::new);
        sub.setUserId(userId); // endpoint connu d'un autre compte : réattribué
        sub.setEndpoint(endpoint);
        sub.setP256dh(p256dh);
        sub.setAuth(auth);
        subscriptions.save(sub);
    }

    @Transactional
    public void unsubscribe(Long userId, String endpoint) {
        subscriptions.deleteByEndpointAndUserId(endpoint, userId);
    }

    public long subscriptionCount(Long userId) { return subscriptions.countByUserId(userId); }

    public void sendTest(Long userId) {
        if (!isConfigured()) return;
        deliver(userId, payload("Notifications activées", "Tu recevras tes rappels sur cet appareil.", "/appointments"));
    }

    // ── Rappels ──────────────────────────────────────────

    @Scheduled(cron = "0 * * * * *", zone = "Europe/Paris")
    public void tick() {
        if (!isConfigured()) return;
        try {
            sendDue(LocalDateTime.now(ZONE));
        } catch (RuntimeException e) { // ne jamais tuer le scheduler
            log.warn("Passage des rappels push en échec", e);
        }
    }

    /** Envoie les rappels dus ; chaque (user, dedupKey) n'est réservé en base qu'une fois, avant l'envoi. */
    public void sendDue(LocalDateTime now) {
        for (Reminder r : dueReminders(now)) {
            if (jdbc.update("INSERT IGNORE INTO push_sent (user_id, dedup_key, sent_at) VALUES (?, ?, NOW(6))",
                    r.userId(), r.dedupKey()) == 1) {
                deliver(r.userId(), payload(r.title(), r.body(), r.url()));
            }
        }
    }

    /** Ce qui est dû à l'instant `now` (heure de Paris), sans tenir compte de ce qui a déjà été envoyé. */
    public List<Reminder> dueReminders(LocalDateTime now) {
        List<Reminder> out = new ArrayList<>();
        LocalDate today = now.toLocalDate();
        subscriptions.findAll().stream().map(PushSubscription::getUserId).distinct().forEach(uid -> {
            User u = users.findById(uid).orElse(null);
            if (u == null) return;
            if (u.isPushApptEnabled()) {
                appointments.findByApptDateBetweenAndUser_IdOrderByApptTimeAsc(today, today.plusDays(1), uid).stream()
                        .filter(a -> a.getApptTime() != null)
                        .forEach(a -> {
                            LocalDateTime start = a.getApptDate().atTime(a.getApptTime());
                            if (start.isBefore(now) || start.isAfter(now.plusMinutes(u.getPushApptMinutes()))) return;
                            long min = Duration.between(now, start).toMinutes();
                            String body = "Rdv dans " + min + " min : " + a.getTitle()
                                    + (a.getLocation() == null || a.getLocation().isBlank() ? "" : " (" + a.getLocation() + ")");
                            out.add(new Reminder(uid, "appt:" + a.getId() + ":" + start, "Rendez-vous", body, "/appointments"));
                        });
            }
            if (now.toLocalTime().isBefore(u.getPushTasksTime())) return;
            if (u.isPushTasksEnabled()) {
                var due = tasks.findByUser_Id(uid).stream()
                        .filter(t -> t.getTasksStatus() != TasksStatus.DONE && t.getTasksDate() != null && !t.getTasksDate().isAfter(today))
                        .toList();
                if (!due.isEmpty()) {
                    String first = due.stream().limit(2).map(t -> String.valueOf(t.getTasksName())).reduce((a, b) -> a + ", " + b).orElse("");
                    out.add(new Reminder(uid, "tasks:" + today, due.size() + (due.size() > 1 ? " tâches aujourd'hui" : " tâche aujourd'hui"),
                            first, "/tasks"));
                }
            }
            if (u.isPushCoachEnabled()) {
                var plan = coachService.getPlan(uid);
                if (plan != null && plan.getDays() != null) {
                    String name = WEEK_DAYS.get(today.getDayOfWeek().getValue() - 1);
                    plan.getDays().stream()
                            .filter(d -> name.equals(d.getDay()) && !"Rest".equals(d.getType()) && !d.isDone())
                            .findFirst()
                            .ifPresent((PlanDayDTO d) -> out.add(new Reminder(uid, "coach:" + today, "Séance du jour",
                                    "Séance du jour : " + d.getLabel(), "/sport/coach")));
                }
            }
        });
        return out;
    }

    private void deliver(Long userId, String payload) {
        for (PushSubscription sub : subscriptions.findByUserId(userId)) {
            int status = sender.send(sub, payload);
            if (status == 404 || status == 410) {
                subscriptions.delete(sub);
            } else if (status < 200 || status >= 300) {
                log.warn("Service push : statut {} pour l'abonnement {}", status, sub.getId());
            }
        }
    }

    private static String payload(String title, String body, String url) {
        var notification = Map.of("title", title, "body", body, "icon", "assets/icons/icon-192.png",
                "data", Map.of("onActionClick", Map.of("default", Map.of("operation", "navigateLastFocusedOrOpen", "url", url))));
        try {
            return MAPPER.writeValueAsString(Map.of("notification", notification));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
