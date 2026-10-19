package io.oltre_backend.appointment;
import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public class appointmentDTO {
    private Long id;
    @NotBlank(message = "Le titre est requis")
    @Size(max = 255, message = "Le titre est trop long")
    private String title;
    @Size(max = 255, message = "La description est trop longue")
    private String description;
    @Size(max = 255, message = "Le lieu est trop long")
    private String location;
    @NotNull(message = "La date est requise")
    private LocalDate apptDate;
    private LocalTime apptTime;
    private LocalTime apptEndTime;
    private appointmentType apptType;
    private AppointmentSource source;

    public appointmentDTO() {}

    public appointmentDTO(Long id, String title, String description, String location,
                          LocalDate apptDate, LocalTime apptTime, LocalTime apptEndTime, appointmentType apptType,
                          AppointmentSource source) {
        this.id = id; this.title = title;
        this.description = description;
        this.location = location;
        this.apptDate = apptDate;
        this.apptTime = apptTime;
        this.apptEndTime = apptEndTime;
        this.apptType = apptType;
        this.source = source;
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

    public AppointmentSource getSource() {
        return source;
    }

    public void setSource(AppointmentSource source) {
        this.source = source;
    }
}
