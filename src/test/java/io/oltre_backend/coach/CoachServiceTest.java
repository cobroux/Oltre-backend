package io.oltre_backend.coach;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.oltre_backend.sport.GarminService;
import io.oltre_backend.user.UserRepository;

class CoachServiceTest {

    private final SportGoalRepository sportGoalRepository = mock(SportGoalRepository.class);
    private final CoachPlanRepository coachPlanRepository = mock(CoachPlanRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final GarminService garminService = mock(GarminService.class);
    private final AnthropicService anthropicService = mock(AnthropicService.class);

    private final CoachService service = new CoachService(
            sportGoalRepository, coachPlanRepository, userRepository, garminService, anthropicService);

    private static final Long USER_ID = 1L;

    private static final String VALID_JSON = """
            {"analysis": "Semaine correcte, continue comme ça.", "days": [
              {"day": "Lundi", "type": "Rest", "label": "Repos", "distanceKm": null},
              {"day": "Mardi", "type": "Run", "label": "Course facile", "distanceKm": 8},
              {"day": "Mercredi", "type": "Rest", "label": "Repos", "distanceKm": null},
              {"day": "Jeudi", "type": "Ride", "label": "Vélo", "distanceKm": 25},
              {"day": "Vendredi", "type": "Rest", "label": "Repos", "distanceKm": null},
              {"day": "Samedi", "type": "Run", "label": "Sortie longue", "distanceKm": 15},
              {"day": "Dimanche", "type": "Rest", "label": "Repos", "distanceKm": null}
            ]}
            """;

    @Test
    void generatePlan_parsesValidResponse_andStoresSevenDaysInOrder() {
        when(sportGoalRepository.findByUser_IdOrderByEventDateAsc(USER_ID)).thenReturn(List.of());
        when(garminService.getActivities(anyLong(), any(), anyInt())).thenReturn(List.of());
        when(coachPlanRepository.findByUser_Id(USER_ID)).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(USER_ID)).thenReturn(null);
        when(anthropicService.complete(any(), any())).thenReturn(VALID_JSON);

        CoachPlanDTO result = service.generatePlan(USER_ID);

        assertEquals("Semaine correcte, continue comme ça.", result.getAnalysis());
        assertEquals(7, result.getDays().size());
        assertEquals("Lundi", result.getDays().get(0).getDay());
        assertEquals("Dimanche", result.getDays().get(6).getDay());
        assertEquals("Run", result.getDays().get(1).getType());
        assertEquals(8.0, result.getDays().get(1).getDistanceKm());
        verify(coachPlanRepository).save(any(CoachPlan.class));
    }

    @Test
    void generatePlan_missingDayInResponse_defaultsToRest() {
        String missingWednesday = """
                {"analysis": "ok", "days": [
                  {"day": "Lundi", "type": "Rest", "label": "Repos", "distanceKm": null},
                  {"day": "Mardi", "type": "Run", "label": "Course facile", "distanceKm": 8},
                  {"day": "Jeudi", "type": "Ride", "label": "Vélo", "distanceKm": 25},
                  {"day": "Vendredi", "type": "Rest", "label": "Repos", "distanceKm": null},
                  {"day": "Samedi", "type": "Run", "label": "Sortie longue", "distanceKm": 15},
                  {"day": "Dimanche", "type": "Rest", "label": "Repos", "distanceKm": null}
                ]}
                """;
        when(sportGoalRepository.findByUser_IdOrderByEventDateAsc(USER_ID)).thenReturn(List.of());
        when(garminService.getActivities(anyLong(), any(), anyInt())).thenReturn(List.of());
        when(coachPlanRepository.findByUser_Id(USER_ID)).thenReturn(Optional.empty());
        when(userRepository.getReferenceById(USER_ID)).thenReturn(null);
        when(anthropicService.complete(any(), any())).thenReturn(missingWednesday);

        CoachPlanDTO result = service.generatePlan(USER_ID);

        assertEquals(7, result.getDays().size());
        PlanDayDTO wednesday = result.getDays().get(2);
        assertEquals("Mercredi", wednesday.getDay());
        assertEquals("Rest", wednesday.getType());
    }

    @Test
    void generatePlan_whenAnthropicNotConfigured_propagatesException() {
        when(sportGoalRepository.findByUser_IdOrderByEventDateAsc(USER_ID)).thenReturn(List.of());
        when(garminService.getActivities(anyLong(), any(), anyInt())).thenReturn(List.of());
        when(anthropicService.complete(any(), any()))
                .thenThrow(new AnthropicNotConfiguredException("pas de clé"));

        assertThrows(AnthropicNotConfiguredException.class, () -> service.generatePlan(USER_ID));
    }

    @Test
    void generatePlan_whenResponseIsNotValidJson_throwsCoachGenerationException() {
        when(sportGoalRepository.findByUser_IdOrderByEventDateAsc(USER_ID)).thenReturn(List.of());
        when(garminService.getActivities(anyLong(), any(), anyInt())).thenReturn(List.of());
        when(anthropicService.complete(any(), any())).thenReturn("ceci n'est pas du JSON");

        assertThrows(CoachGenerationException.class, () -> service.generatePlan(USER_ID));
    }

    @Test
    void toggleDay_flipsDoneState_andPersists() {
        CoachPlan plan = new CoachPlan();
        plan.setGeneratedAt(java.time.LocalDateTime.now());
        plan.setAnalysis("ok");
        plan.setDaysJson("""
                [{"day":"Lundi","type":"Rest","label":"Repos","distanceKm":null,"done":false},
                 {"day":"Mardi","type":"Run","label":"Course facile","distanceKm":8,"done":false}]
                """);
        when(coachPlanRepository.findByUser_Id(USER_ID)).thenReturn(Optional.of(plan));

        CoachPlanDTO result = service.toggleDay(USER_ID, 1);

        assertTrue(result.getDays().get(1).isDone());
        assertFalse(result.getDays().get(0).isDone());
        verify(coachPlanRepository).save(plan);
    }

    @Test
    void addGoal_computesNothingButPersistsFields() {
        SportGoalDTO dto = new SportGoalDTO();
        dto.setGoalType(SportGoalType.Marathon);
        dto.setName("Marathon de Paris");
        dto.setEventDate(LocalDate.of(2026, 4, 12));
        dto.setTargetTime("3h45");

        when(userRepository.getReferenceById(USER_ID)).thenReturn(null);
        when(sportGoalRepository.save(any(SportGoal.class))).thenAnswer(inv -> inv.getArgument(0));

        SportGoalDTO result = service.addGoal(USER_ID, dto);

        assertEquals("Marathon de Paris", result.getName());
        assertEquals(SportGoalType.Marathon, result.getGoalType());
        assertEquals("3h45", result.getTargetTime());
    }
}
