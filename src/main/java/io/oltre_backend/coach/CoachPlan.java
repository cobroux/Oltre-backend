package io.oltre_backend.coach;

import java.time.LocalDateTime;

import io.oltre_backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

// Un seul plan stocké par utilisateur - écrasé à chaque régénération plutôt
// que gardé en historique, pour rester simple (pas de besoin exprimé de
// consulter d'anciens plans).
@Entity
@Table(name = "coach_plans")
public class CoachPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Lob
    @Column(name = "analysis", nullable = false)
    private String analysis;

    // Sérialisé en JSON (liste de 7 PlanDayDTO) plutôt qu'une table à part :
    // pas besoin d'interroger les jours individuellement côté base, et ça
    // évite une table de plus pour une donnée qui n'est jamais lue que dans
    // son ensemble.
    @Lob
    @Column(name = "days_json", nullable = false)
    private String daysJson;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    public CoachPlan() {}

    public Long getId() { return id; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }
    public String getDaysJson() { return daysJson; }
    public void setDaysJson(String daysJson) { this.daysJson = daysJson; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
