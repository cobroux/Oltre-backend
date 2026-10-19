package io.oltre_backend.coach;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SportGoalDTO {

    private Long id;
    @NotNull(message = "Le type d'objectif est requis")
    private SportGoalType goalType;
    @NotBlank(message = "Le nom est requis")
    @Size(max = 255, message = "Le nom est trop long")
    private String name;
    @NotNull(message = "La date est requise")
    private LocalDate eventDate;
    @Size(max = 255, message = "Le temps cible est trop long")
    private String targetTime;

    public SportGoalDTO() {}

    public SportGoalDTO(SportGoal g) {
        this.id = g.getId();
        this.goalType = g.getGoalType();
        this.name = g.getName();
        this.eventDate = g.getEventDate();
        this.targetTime = g.getTargetTime();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SportGoalType getGoalType() { return goalType; }
    public void setGoalType(SportGoalType goalType) { this.goalType = goalType; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public String getTargetTime() { return targetTime; }
    public void setTargetTime(String targetTime) { this.targetTime = targetTime; }
}
