package io.oltre_backend.coach;

import java.time.LocalDate;

import io.oltre_backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sport_goals")
public class SportGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false)
    private SportGoalType goalType;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "target_time")
    private String targetTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public SportGoal() {}

    public SportGoal(SportGoalType goalType, String name, LocalDate eventDate, String targetTime) {
        this.goalType = goalType;
        this.name = name;
        this.eventDate = eventDate;
        this.targetTime = targetTime;
    }

    public Long getId() { return id; }
    public SportGoalType getGoalType() { return goalType; }
    public void setGoalType(SportGoalType goalType) { this.goalType = goalType; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public String getTargetTime() { return targetTime; }
    public void setTargetTime(String targetTime) { this.targetTime = targetTime; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
