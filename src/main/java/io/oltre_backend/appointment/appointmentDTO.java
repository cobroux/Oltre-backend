package io.oltre_backend.appointment;
import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class appointmentDTO {
    private Long id;
    private String title;
    private String description;
    private String location;
    private LocalDate apptDate;
    private LocalTime apptTime;
    private appointmentType apptType;

    public appointmentDTO() {}
    
    public appointmentDTO(Long id, String title, String description, String location,
                          LocalDate apptDate, LocalTime apptTime, appointmentType apptType) {
        this.id = id; this.title = title; 
        this.description = description;
        this.location = location; 
        this.apptDate = apptDate;
        this.apptTime = apptTime; 
        this.apptType = apptType;
    }

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
}
