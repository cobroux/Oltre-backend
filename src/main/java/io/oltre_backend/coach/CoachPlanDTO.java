package io.oltre_backend.coach;

import java.time.LocalDateTime;
import java.util.List;

public class CoachPlanDTO {

    private LocalDateTime generatedAt;
    private String analysis;
    private List<PlanDayDTO> days;

    public CoachPlanDTO() {}

    public CoachPlanDTO(LocalDateTime generatedAt, String analysis, List<PlanDayDTO> days) {
        this.generatedAt = generatedAt;
        this.analysis = analysis;
        this.days = days;
    }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }
    public List<PlanDayDTO> getDays() { return days; }
    public void setDays(List<PlanDayDTO> days) { this.days = days; }
}
