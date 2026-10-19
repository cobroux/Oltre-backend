package io.oltre_backend.coach;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import io.oltre_backend.appointment.AppointmentSource;
import io.oltre_backend.appointment.appointmentService;
import io.oltre_backend.sport.GarminActivityDTO;
import io.oltre_backend.sport.GarminAuthException;
import io.oltre_backend.sport.GarminService;
import io.oltre_backend.sport.GarminServiceUnavailableException;
import io.oltre_backend.user.UserRepository;

@Service
public class CoachService {

    private static final Logger log = LoggerFactory.getLogger(CoachService.class);
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final List<String> WEEK_DAYS = List.of("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche");

    private final SportGoalRepository sportGoalRepository;
    private final CoachPlanRepository coachPlanRepository;
    private final UserRepository userRepository;
    private final GarminService garminService;
    private final GeminiService geminiService;
    private final appointmentService appointmentService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CoachService(
            SportGoalRepository sportGoalRepository,
            CoachPlanRepository coachPlanRepository,
            UserRepository userRepository,
            GarminService garminService,
            GeminiService geminiService,
            appointmentService appointmentService) {
        this.sportGoalRepository = sportGoalRepository;
        this.coachPlanRepository = coachPlanRepository;
        this.userRepository = userRepository;
        this.garminService = garminService;
        this.geminiService = geminiService;
        this.appointmentService = appointmentService;
    }

    // ── Objectifs ─────────────────────────────────────────

    public List<SportGoalDTO> getGoals(Long userId) {
        return sportGoalRepository.findByUser_IdOrderByEventDateAsc(userId).stream()
                .map(SportGoalDTO::new)
                .toList();
    }

    public SportGoalDTO addGoal(Long userId, SportGoalDTO dto) {
        SportGoal goal = new SportGoal(dto.getGoalType(), dto.getName(), dto.getEventDate(), dto.getTargetTime());
        goal.setUser(userRepository.getReferenceById(userId));
        return new SportGoalDTO(sportGoalRepository.save(goal));
    }

    public void deleteGoal(Long id, Long userId) {
        sportGoalRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        sportGoalRepository.deleteById(id);
    }

    // ── Plan ──────────────────────────────────────────────

    public CoachPlanDTO getPlan(Long userId) {
        return coachPlanRepository.findByUser_Id(userId)
                .map(this::toDto)
                .orElse(null);
    }

    public CoachPlanDTO generatePlan(Long userId) {
        List<SportGoal> goals = sportGoalRepository.findByUser_IdOrderByEventDateAsc(userId);
        List<GarminActivityDTO> recentActivities = fetchRecentActivities(userId);

        String systemPrompt = """
                Tu es un coach sportif pour un·e coureur·se/triathlète amateur·e. On te donne ses \
                objectifs (courses à venir) et son historique d'activités des 4 dernières semaines \
                (Garmin). Réponds UNIQUEMENT avec un objet JSON valide, sans texte autour, sans bloc \
                markdown, de la forme exacte suivante :
                {"analysis": "2-4 phrases en français sur la semaine écoulée et le contexte par rapport aux objectifs", \
                "days": [{"day": "Lundi", "type": "Rest|Run|Ride|Swim|Other", "label": "libellé court en français", "distanceKm": nombre ou null}, ... 7 entrées, une par jour de Lundi à Dimanche]}
                Adapte le volume et l'intensité à la proximité de l'objectif le plus proche (ex: réduire \
                le volume si l'échéance est dans moins de 2 semaines). Ne mets jamais de séance de \
                musculation dans ce plan (type doit être Rest, Run, Ride, Swim ou Other) - le \
                renforcement musculaire est géré séparément, en option.""";

        String userPrompt = buildUserPrompt(goals, recentActivities);

        String raw = geminiService.complete(systemPrompt, userPrompt);
        GeneratedPlan generated = parseGeneratedPlan(raw);

        CoachPlan plan = coachPlanRepository.findByUser_Id(userId).orElseGet(() -> {
            CoachPlan p = new CoachPlan();
            p.setUser(userRepository.getReferenceById(userId));
            return p;
        });
        plan.setGeneratedAt(LocalDateTime.now());
        plan.setAnalysis(generated.analysis);
        List<PlanDayDTO> days = normalizeDays(generated.days);
        plan.setDaysJson(writeDaysJson(days));
        coachPlanRepository.save(plan);

        syncPlanToCalendar(userId, days);

        return toDto(plan);
    }

    // Reflète le plan dans le calendrier : une entrée par jour avec séance
    // (les jours "Repos" ne créent rien, pour ne pas l'encombrer). On
    // commence par retirer les entrées COACH_PLAN déjà présentes sur cette
    // semaine avant de réinsérer, pour qu'une régénération qui change ou
    // retire une séance ne laisse pas une entrée périmée - les entrées que
    // l'utilisateur a éditées entre-temps sont déjà passées en MANUAL
    // (appointmentService.update) et ne sont donc jamais supprimées ici.
    private void syncPlanToCalendar(Long userId, List<PlanDayDTO> days) {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDate weekEnd = nextMonday.plusDays(6);

        appointmentService.deleteBySourceForWeek(userId, AppointmentSource.COACH_PLAN, nextMonday, weekEnd);

        for (int i = 0; i < days.size(); i++) {
            PlanDayDTO day = days.get(i);
            if ("Rest".equals(day.getType())) continue;

            LocalDate date = nextMonday.plusDays(i);
            String description = day.getDistanceKm() != null
                    ? String.format("Suggestion du Coach IA · %.1f km", day.getDistanceKm())
                    : "Suggestion du Coach IA";
            appointmentService.upsertFromSource(
                    userId, day.getLabel(), description, date, null,
                    AppointmentSource.COACH_PLAN, date.toString());
        }
    }

    public CoachPlanDTO toggleDay(Long userId, int dayIndex) {
        if (dayIndex < 0 || dayIndex > 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Index de jour invalide (0-6 attendu).");
        }
        CoachPlan plan = coachPlanRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucun plan généré."));

        List<PlanDayDTO> days = readDaysJson(plan.getDaysJson());
        if (dayIndex >= days.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Index de jour invalide.");
        }
        days.get(dayIndex).setDone(!days.get(dayIndex).isDone());
        plan.setDaysJson(writeDaysJson(days));
        coachPlanRepository.save(plan);

        return toDto(plan);
    }

    // ── Aides ─────────────────────────────────────────────

    private List<GarminActivityDTO> fetchRecentActivities(Long userId) {
        try {
            LocalDate fourWeeksAgo = LocalDate.now().minusWeeks(4);
            return garminService.getActivities(userId, fourWeeksAgo.format(ISO_DATE), 100);
        } catch (GarminAuthException e) {
            // Compte Garmin non connecté : on génère quand même un plan, juste
            // sans historique d'activités à analyser.
            return List.of();
        } catch (GarminServiceUnavailableException e) {
            // Garmin éteint : le plan se génère quand même, sans historique.
            log.warn("Garmin indisponible, plan généré sans activités récentes");
            return List.of();
        }
    }

    private String buildUserPrompt(List<SportGoal> goals, List<GarminActivityDTO> activities) {
        StringBuilder sb = new StringBuilder();
        LocalDate today = LocalDate.now();

        sb.append("Objectifs :\n");
        if (goals.isEmpty()) {
            sb.append("- Aucun objectif renseigné pour l'instant, propose un plan d'entretien général.\n");
        } else {
            for (SportGoal g : goals) {
                long daysUntil = ChronoUnit.DAYS.between(today, g.getEventDate());
                sb.append("- ").append(g.getGoalType()).append(" \"").append(g.getName()).append("\" le ")
                        .append(g.getEventDate()).append(" (dans ").append(daysUntil).append(" jours)");
                if (g.getTargetTime() != null && !g.getTargetTime().isBlank()) {
                    sb.append(", objectif chrono : ").append(g.getTargetTime());
                }
                sb.append("\n");
            }
        }

        sb.append("\nActivités des 4 dernières semaines :\n");
        if (activities.isEmpty()) {
            sb.append("- Aucune activité récente trouvée (compte Garmin non connecté ou pas de données).\n");
        } else {
            for (GarminActivityDTO a : activities) {
                sb.append("- ").append(a.getStartLocal()).append(" : ").append(a.getSportType());
                if (a.getDistance() != null && a.getDistance() > 0) {
                    sb.append(", ").append(String.format("%.1f", a.getDistance() / 1000.0)).append(" km");
                }
                if (a.getMovingTime() != null && a.getMovingTime() > 0) {
                    sb.append(", ").append(a.getMovingTime() / 60).append(" min");
                }
                sb.append("\n");
            }
        }

        sb.append("\nAujourd'hui : ").append(today).append(". Propose le plan pour la semaine prochaine (Lundi à Dimanche).");
        return sb.toString();
    }

    // Garantit toujours 7 jours dans le bon ordre, même si le modèle en a
    // oublié un ou les a rendus dans le désordre - mieux vaut un jour par
    // défaut en "Repos" qu'un plan qui plante ou qu'une liste à 5 jours.
    private List<PlanDayDTO> normalizeDays(List<PlanDayDTO> raw) {
        return WEEK_DAYS.stream()
                .map(day -> raw.stream()
                        .filter(d -> day.equalsIgnoreCase(d.getDay()))
                        .findFirst()
                        .map(d -> new PlanDayDTO(day, d.getType(), d.getLabel(), d.getDistanceKm(), false))
                        .orElseGet(() -> new PlanDayDTO(day, "Rest", "Repos", null, false)))
                .toList();
    }

    private GeneratedPlan parseGeneratedPlan(String raw) {
        String cleaned = raw.trim();
        // Le modèle respecte presque toujours la consigne "JSON seul", mais
        // s'il entoure quand même sa réponse d'un bloc ```json ... ```, on
        // l'enlève plutôt que d'échouer bêtement sur le parsing.
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(json)?", "").replaceFirst("```$", "").trim();
        }
        try {
            return objectMapper.readValue(cleaned, GeneratedPlan.class);
        } catch (Exception e) {
            throw new CoachGenerationException("Le Coach IA a renvoyé une réponse inattendue, réessaie.", e);
        }
    }

    private List<PlanDayDTO> readDaysJson(String json) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, PlanDayDTO.class));
        } catch (Exception e) {
            throw new CoachGenerationException("Plan stocké illisible.", e);
        }
    }

    private String writeDaysJson(List<PlanDayDTO> days) {
        try {
            return objectMapper.writeValueAsString(days);
        } catch (Exception e) {
            throw new CoachGenerationException("Impossible de sauvegarder le plan.", e);
        }
    }

    private CoachPlanDTO toDto(CoachPlan plan) {
        return new CoachPlanDTO(plan.getGeneratedAt(), plan.getAnalysis(), readDaysJson(plan.getDaysJson()));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeneratedPlan {
        public String analysis;
        public List<PlanDayDTO> days;
    }
}
