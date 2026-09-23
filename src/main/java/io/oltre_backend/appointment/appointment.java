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
    
    @Enumerated(EnumType.STRING)
    @Column(name = "appt_type")
    private appointmentType apptType;

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
    
    public appointmentType getApptType() { 
        return apptType; 
    }
    
    public void setApptType(appointmentType apptType) {
        this.apptType = apptType;
    }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }
}
