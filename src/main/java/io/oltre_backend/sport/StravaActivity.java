package io.oltre_backend.sport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StravaActivity {

    private Long id;
    private String name;
    private String description;

    @JsonProperty("sport_type")
    private String sportType;

    @JsonProperty("start_date_local")
    private String startLocal;

    private Double distance;

    @JsonProperty("moving_time")
    private Integer movingTime;

    @JsonProperty("elapsed_time")
    private Integer elapsedTime;

    @JsonProperty("total_elevation_gain")
    private Double elevationGain;

    @JsonProperty("average_speed")
    private Double avgSpeed;

    @JsonProperty("max_speed")
    private Double maxSpeed;

    private Double calories;

    @JsonProperty("average_cadence")
    private Double avgCadence;

    @JsonProperty("suffer_score")
    private Integer relativeEffort;

    @JsonProperty("kudos_count")
    private Integer kudosCount;

    @JsonProperty("achievement_count")
    private Integer achievementCount;

    @JsonProperty("pr_count")
    private Integer prCount;

    @JsonProperty("gear_id")
    private String gearId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSportType() { return sportType; }
    public void setSportType(String sportType) { this.sportType = sportType; }
    public String getStartLocal() { return startLocal; }
    public void setStartLocal(String startLocal) { this.startLocal = startLocal; }
    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }
    public Integer getMovingTime() { return movingTime; }
    public void setMovingTime(Integer movingTime) { this.movingTime = movingTime; }
    public Integer getElapsedTime() { return elapsedTime; }
    public void setElapsedTime(Integer elapsedTime) { this.elapsedTime = elapsedTime; }
    public Double getElevationGain() { return elevationGain; }
    public void setElevationGain(Double elevationGain) { this.elevationGain = elevationGain; }
    public Double getAvgSpeed() { return avgSpeed; }
    public void setAvgSpeed(Double avgSpeed) { this.avgSpeed = avgSpeed; }
    public Double getMaxSpeed() { return maxSpeed; }
    public void setMaxSpeed(Double maxSpeed) { this.maxSpeed = maxSpeed; }
    public Double getCalories() { return calories; }
    public void setCalories(Double calories) { this.calories = calories; }
    public Double getAvgCadence() { return avgCadence; }
    public void setAvgCadence(Double avgCadence) { this.avgCadence = avgCadence; }
    public Integer getRelativeEffort() { return relativeEffort; }
    public void setRelativeEffort(Integer relativeEffort) { this.relativeEffort = relativeEffort; }
    public Integer getKudosCount() { return kudosCount; }
    public void setKudosCount(Integer kudosCount) { this.kudosCount = kudosCount; }
    public Integer getAchievementCount() { return achievementCount; }
    public void setAchievementCount(Integer achievementCount) { this.achievementCount = achievementCount; }
    public Integer getPrCount() { return prCount; }
    public void setPrCount(Integer prCount) { this.prCount = prCount; }
    public String getGearId() { return gearId; }
    public void setGearId(String gearId) { this.gearId = gearId; }
}
