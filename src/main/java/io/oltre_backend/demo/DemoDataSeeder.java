package io.oltre_backend.demo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.oltre_backend.appointment.appointment;
import io.oltre_backend.appointment.appointmentRepository;
import io.oltre_backend.appointment.appointmentType;
import io.oltre_backend.coach.CoachPlanRepository;
import io.oltre_backend.coach.SportGoal;
import io.oltre_backend.coach.SportGoalRepository;
import io.oltre_backend.coach.SportGoalType;
import io.oltre_backend.expenses.Expenses;
import io.oltre_backend.expenses.ExpensesRepository;
import io.oltre_backend.expenses.RecType;
import io.oltre_backend.meal.Meal;
import io.oltre_backend.meal.MealRepository;
import io.oltre_backend.meal.MealType;
import io.oltre_backend.tasks.Tasks;
import io.oltre_backend.tasks.TasksRepository;
import io.oltre_backend.tasks.task_enum.TasksPriority;
import io.oltre_backend.tasks.task_enum.TasksStatus;
import io.oltre_backend.tasks.task_enum.TasksType;
import io.oltre_backend.user.User;
import io.oltre_backend.user.UserRepository;

/** Vide les tables puis recrée un faux athlète, au démarrage et chaque nuit (dates relatives au jour). */
@Component
@Profile("demo")
@EnableScheduling
public class DemoDataSeeder {

    public static final String EMAIL = "demo@oltre.dev";
    public static final String PASSWORD = "demo-oltre-2026"; // public, affiché sur la page de connexion

    private final UserRepository users;
    private final ExpensesRepository expenses;
    private final TasksRepository tasks;
    private final MealRepository meals;
    private final appointmentRepository appointments;
    private final SportGoalRepository goals;
    private final CoachPlanRepository plans;
    private final PasswordEncoder encoder;

    public DemoDataSeeder(UserRepository users, ExpensesRepository expenses, TasksRepository tasks,
                          MealRepository meals, appointmentRepository appointments,
                          SportGoalRepository goals, CoachPlanRepository plans, PasswordEncoder encoder) {
        this.users = users;
        this.expenses = expenses;
        this.tasks = tasks;
        this.meals = meals;
        this.appointments = appointments;
        this.goals = goals;
        this.plans = plans;
        this.encoder = encoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 4 * * *", zone = "Europe/Paris")
    @Transactional
    public void reset() {
        // Garde-fou : jamais de purge d'une base qui contient un autre utilisateur que l'athlète de démo.
        if (users.findAll().stream().anyMatch(x -> !EMAIL.equals(x.getEmail())))
            throw new IllegalStateException("Base non démo : purge refusée");
        plans.deleteAllInBatch();
        goals.deleteAllInBatch();
        tasks.deleteAllInBatch();
        expenses.deleteAllInBatch();
        meals.deleteAllInBatch();
        appointments.deleteAllInBatch();
        users.deleteAllInBatch();

        LocalDate today = LocalDate.now(ZoneId.of("Europe/Paris"));
        User u = new User();
        u.setUsername("Démo");
        u.setEmail(EMAIL);
        u.setPassword(encoder.encode(PASSWORD));
        u.setAge(30);
        u.setBirthDate(today.minusYears(30));
        u = users.save(u);

        // Dépenses sur 6 mois : abonnements mensuels + une dépense annuelle.
        for (int m = 0; m < 6; m++) {
            expense(u, "Loyer", "650.00", RecType.Monthly, today.minusMonths(m).withDayOfMonth(1), m == 0 ? null : today.minusMonths(m).withDayOfMonth(28));
            expense(u, "Salle de sport", "29.90", RecType.Monthly, today.minusMonths(m).withDayOfMonth(5), m == 0 ? null : today.minusMonths(m).withDayOfMonth(27));
        }
        expense(u, "Assurance", "240.00", RecType.Yearly, today.minusMonths(3), null);

        task(u, "Réserver les dossards", TasksType.SPORT, TasksPriority.HIGH, TasksStatus.PENDING, today.plusDays(2));
        task(u, "Faire les courses", TasksType.PERSO, TasksPriority.MEDIUM, TasksStatus.PENDING, today);
        task(u, "Préparer la présentation", TasksType.TRAVAIL, TasksPriority.HIGH, TasksStatus.PENDING, today.plusDays(1));
        task(u, "Changer les chaussures de course", TasksType.SPORT, TasksPriority.LOW, TasksStatus.DONE, today.minusDays(3));

        MealType[] types = {MealType.PETIT_DEJ, MealType.DEJEUNER, MealType.DINER};
        String[] names = {"Porridge aux fruits", "Poulet, riz et brocolis", "Pâtes complètes au pesto"};
        int[] kcal = {420, 680, 720};
        for (int d = 0; d < 7; d++) {
            for (int i = 0; i < 3; i++) {
                Meal m = new Meal();
                m.setMealName(names[i]);
                m.setMealType(types[i]);
                m.setMealDate(today.minusDays(today.getDayOfWeek().getValue() - 1L).plusDays(d));
                m.setCalories(kcal[i]);
                m.setProteinG(20.0 + i * 8);
                m.setCarbsG(55.0 + i * 5);
                m.setFatG(12.0 + i * 3);
                m.setUser(u);
                meals.save(m);
            }
        }

        appointment(u, "Visite médicale du sport", appointmentType.MEDICAL, today.plusDays(4));
        appointment(u, "Sortie longue avec le club", appointmentType.SPORT, today.plusDays(6));
        appointment(u, "Point d'équipe", appointmentType.TRAVAIL, today.plusDays(2));

        goal(u, SportGoalType.SemiMarathon, "Semi-marathon de démonstration", today.plusWeeks(10), "1:50:00");
        goal(u, SportGoalType.TenK, "10 km de démonstration", today.plusWeeks(4), "50:00");
    }

    private void expense(User u, String name, String amount, RecType type, LocalDate start, LocalDate end) {
        Expenses e = new Expenses();
        e.setExpensesName(name);
        e.setAmount(new BigDecimal(amount));
        e.setRecType(type);
        e.setStartDate(start);
        e.setEndDate(end);
        e.setUser(u);
        expenses.save(e);
    }

    private void task(User u, String name, TasksType type, TasksPriority p, TasksStatus s, LocalDate date) {
        Tasks t = new Tasks();
        t.setTasksName(name);
        t.setTasksType(type);
        t.setTasksPriority(p);
        t.setTasksStatus(s);
        t.setTasksDate(date);
        t.setUser(u);
        tasks.save(t);
    }

    private void appointment(User u, String title, appointmentType type, LocalDate date) {
        appointment a = new appointment();
        a.setTitle(title);
        a.setApptType(type);
        a.setApptDate(date);
        a.setApptTime(java.time.LocalTime.of(18, 0));
        a.setUser(u);
        appointments.save(a);
    }

    private void goal(User u, SportGoalType type, String name, LocalDate date, String target) {
        SportGoal g = new SportGoal();
        g.setGoalType(type);
        g.setName(name);
        g.setEventDate(date);
        g.setTargetTime(target);
        g.setUser(u);
        goals.save(g);
    }
}
