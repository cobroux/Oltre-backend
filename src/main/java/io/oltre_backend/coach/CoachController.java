package io.oltre_backend.coach;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.oltre_backend.auth.CurrentUser;

@RestController
@RequestMapping("/api/coach")
public class CoachController {

    private final CoachService coachService;

    public CoachController(CoachService coachService) {
        this.coachService = coachService;
    }

    @GetMapping("/goals")
    public List<SportGoalDTO> getGoals() {
        return coachService.getGoals(CurrentUser.id());
    }

    @PostMapping("/goals")
    public SportGoalDTO addGoal(@RequestBody SportGoalDTO dto) {
        return coachService.addGoal(CurrentUser.id(), dto);
    }

    @DeleteMapping("/goals/{id}")
    public void deleteGoal(@PathVariable Long id) {
        coachService.deleteGoal(id, CurrentUser.id());
    }

    @GetMapping("/plan")
    public ResponseEntity<CoachPlanDTO> getPlan() {
        CoachPlanDTO plan = coachService.getPlan(CurrentUser.id());
        return plan != null ? ResponseEntity.ok(plan) : ResponseEntity.noContent().build();
    }

    @PostMapping("/plan/generate")
    public CoachPlanDTO generatePlan() {
        return coachService.generatePlan(CurrentUser.id());
    }

    @PostMapping("/plan/days/{index}/toggle")
    public CoachPlanDTO toggleDay(@PathVariable int index) {
        return coachService.toggleDay(CurrentUser.id(), index);
    }

    @ExceptionHandler(CoachAiNotConfiguredException.class)
    public ResponseEntity<Map<String, String>> handleNotConfigured(CoachAiNotConfiguredException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(CoachGenerationException.class)
    public ResponseEntity<Map<String, String>> handleGenerationError(CoachGenerationException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("error", e.getMessage()));
    }
}
