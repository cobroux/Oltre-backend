package io.oltre_backend.sport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GarminActivity {

    @JsonProperty("activityId")
    private Long id;

    @JsonProperty("activityName")
    private String name;

    private String description;

    @JsonProperty("activityType")
    private ActivityType activityType;

    @JsonProperty("startTimeLocal")
    private String startLocal;

    private Double distance;

    private Double duration;

    @JsonProperty("elevationGain")
    private Double elevationGain;

    @JsonProperty("averageSpeed")
    private Double avgSpeed;

    @JsonProperty("maxSpeed")
    private Double maxSpeed;

    private Double calories;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSportType() { return activityType != null ? activityType.getTypeKey() : null; }
    public void setActivityType(ActivityType activityType) { this.activityType = activityType; }
    public String getStartLocal() { return startLocal; }
    public void setStartLocal(String startLocal) { this.startLocal = startLocal; }
    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }
    public Double getDuration() { return duration; }
    public void setDuration(Double duration) { this.duration = duration; }
    public Double getElevationGain() { return elevationGain; }
    public void setElevationGain(Double elevationGain) { this.elevationGain = elevationGain; }
    public Double getAvgSpeed() { return avgSpeed; }
    public void setAvgSpeed(Double avgSpeed) { this.avgSpeed = avgSpeed; }
    public Double getMaxSpeed() { return maxSpeed; }
    public void setMaxSpeed(Double maxSpeed) { this.maxSpeed = maxSpeed; }
    public Double getCalories() { return calories; }
    public void setCalories(Double calories) { this.calories = calories; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ActivityType {
        @JsonProperty("typeKey")
        private String typeKey;
        public String getTypeKey() { return typeKey; }
        public void setTypeKey(String typeKey) { this.typeKey = typeKey; }
    }
}
