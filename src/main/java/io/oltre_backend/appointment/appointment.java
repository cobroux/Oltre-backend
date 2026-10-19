package io.oltre_backend.appointment;
import java.time.LocalDate;
import java.time.LocalTime;

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

import io.oltre_backend.user.User;

@Entity
@Table(name = "appointments")
public class appointment {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description")
    private String description;
   
    @Column(name = "location")
    private String location;
    
    @Column(name = "appt_date", nullable = false)
    private LocalDate apptDate;
   
    @Column(name = "appt_time")
    private LocalTime apptTime;

    @Column(name = "appt_end_time")
    private LocalTime apptEndTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "appt_type")
    private appointmentType apptType;

    // D'où vient ce rendez-vous (voir AppointmentSource) - permet de
    // synchroniser les activités Garmin et le plan du Coach IA dans le
    // calendrier sans créer de doublons à chaque rafraîchissement : on
    // retrouve l'entrée existante via (user, source, sourceRef) et on la met
    // à jour plutôt que d'en recréer une.
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private AppointmentSource source = AppointmentSource.MANUAL;

    @Column(name = "source_ref")
    private String sourceRef;

    // Vrai dès que l'utilisateur a modifié l'entrée : la synchro ne l'écrase plus.
    @Column(name = "edited", nullable = false)
    private boolean edited = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public appointment() {}
    
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }
    
    public String getTitle() { 
        return title; 
    }
    
    public void setTitle(String title) { 
        this.title = title; 
    }
    
    public String getDescription() { 
        return description; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }
    
    public String getLocation() { 
        return location; 
    }
    
    public void setLocation(String location) {
         this.location = location; 
        }
    
    public LocalDate getApptDate() { 
        return apptDate; 
    }
    
    public void setApptDate(LocalDate apptDate) { 
        this.apptDate = apptDate; 
    }
    
    public LocalTime getApptTime() {
         return apptTime; 
        }
    
    public void setApptTime(LocalTime apptTime) {
        this.apptTime = apptTime;
     }

    public LocalTime getApptEndTime() {
        return apptEndTime;
    }

    public void setApptEndTime(LocalTime apptEndTime) {
        this.apptEndTime = apptEndTime;
    }

    public appointmentType getApptType() {
        return apptType; 
    }
    
    public void setApptType(appointmentType apptType) {
        this.apptType = apptType;
    }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public AppointmentSource getSource() { return source; }

    public void setSource(AppointmentSource source) { this.source = source; }

    public String getSourceRef() { return sourceRef; }

    public void setSourceRef(String sourceRef) { this.sourceRef = sourceRef; }

    public boolean isEdited() { return edited; }

    public void setEdited(boolean edited) { this.edited = edited; }
}
