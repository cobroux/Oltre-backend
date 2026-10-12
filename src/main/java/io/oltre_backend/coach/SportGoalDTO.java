package io.oltre_backend.coach;

import java.time.LocalDate;

public class SportGoalDTO {

    private Long id;
    private SportGoalType goalType;
    private String name;
    private LocalDate eventDate;
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
