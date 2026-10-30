package io.oltre_backend.user;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.oltre_backend.appointment.AppointmentSource;
import io.oltre_backend.appointment.appointmentService;
import io.oltre_backend.coach.CoachService;
import io.oltre_backend.expenses.ExpensesService;
import io.oltre_backend.meal.MealService;
import io.oltre_backend.tasks.TasksService;

// Export RGPD : uniquement des DTO, jamais l'entité User (mot de passe haché,
// jeton du flux .ics). Chaque source est filtrée par l'id de l'utilisateur courant.
@Service
public class UserExportService {

    private final UserRepository userRepository;
    private final appointmentService appointmentService;
    private final TasksService tasksService;
    private final MealService mealService;
    private final ExpensesService expensesService;
    private final CoachService coachService;

    public UserExportService(UserRepository userRepository, appointmentService appointmentService,
                             TasksService tasksService, MealService mealService,
                             ExpensesService expensesService, CoachService coachService) {
        this.userRepository = userRepository;
        this.appointmentService = appointmentService;
        this.tasksService = tasksService;
        this.mealService = mealService;
        this.expensesService = expensesService;
        this.coachService = coachService;
    }

    public Map<String, Object> export(Long id) {
        User user = userRepository.findById(id).orElseThrow();
        var appointments = appointmentService.getAll(id);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("exportedAt", Instant.now().toString());
        Map<String, Object> profile = new LinkedHashMap<>(); // Map.of refuse les null
        profile.put("username", user.getUsername());
        profile.put("email", user.getEmail());
        profile.put("age", user.getAge());
        profile.put("birthDate", user.getBirthDate());
        out.put("profile", profile);
        // Préférences seulement : jamais les clés d'abonnement push des appareils.
        out.put("notificationPreferences", Map.of(
                "apptEnabled", user.isPushApptEnabled(), "apptMinutesBefore", user.getPushApptMinutes(),
                "tasksEnabled", user.isPushTasksEnabled(), "tasksTime", user.getPushTasksTime().toString(),
                "coachEnabled", user.isPushCoachEnabled()));
        out.put("appointments", appointments);
        out.put("tasks", tasksService.getTasks(id));
        out.put("meals", mealService.getAll(id));
        out.put("expenses", expensesService.getExpensess(id));
        out.put("sportGoals", coachService.getGoals(id));
        out.put("coachPlan", coachService.getPlan(id));
        // Seules les activités Garmin stockées en base (rdv importés) : le sidecar n'est pas appelé.
        out.put("garminActivities", appointments.stream().filter(a -> a.getSource() == AppointmentSource.GARMIN).toList());
        return out;
    }
}
