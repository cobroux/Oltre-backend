package io.oltre_backend.sport;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GarminActivityDetailDTO {

    private String activityId;
    private String name;
    private String sportType;
    private String startLocal;
    private Double distance;
    private Double duration;
    private Double elevationGain;
    private Double elevationLoss;
    private Double calories;
    private Double averageHR;
    private Double maxHR;
    private Double averageSpeed;
    private Double maxSpeed;
    private List<Point> polyline;
    private List<Split> splits;
    private List<ExerciseSet> exerciseSets;

    public String getActivityId() { return activityId; }
    public void setActivityId(String activityId) { this.activityId = activityId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSportType() { return sportType; }
    public void setSportType(String sportType) { this.sportType = sportType; }
    public String getStartLocal() { return startLocal; }
    public void setStartLocal(String startLocal) { this.startLocal = startLocal; }
    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }
    public Double getDuration() { return duration; }
    public void setDuration(Double duration) { this.duration = duration; }
    public Double getElevationGain() { return elevationGain; }
    public void setElevationGain(Double elevationGain) { this.elevationGain = elevationGain; }
    public Double getElevationLoss() { return elevationLoss; }
    public void setElevationLoss(Double elevationLoss) { this.elevationLoss = elevationLoss; }
    public Double getCalories() { return calories; }
    public void setCalories(Double calories) { this.calories = calories; }
    public Double getAverageHR() { return averageHR; }
    public void setAverageHR(Double averageHR) { this.averageHR = averageHR; }
    public Double getMaxHR() { return maxHR; }
    public void setMaxHR(Double maxHR) { this.maxHR = maxHR; }
    public Double getAverageSpeed() { return averageSpeed; }
    public void setAverageSpeed(Double averageSpeed) { this.averageSpeed = averageSpeed; }
    public Double getMaxSpeed() { return maxSpeed; }
    public void setMaxSpeed(Double maxSpeed) { this.maxSpeed = maxSpeed; }
    public List<Point> getPolyline() { return polyline; }
    public void setPolyline(List<Point> polyline) { this.polyline = polyline; }
    public List<Split> getSplits() { return splits; }
    public void setSplits(List<Split> splits) { this.splits = splits; }
    public List<ExerciseSet> getExerciseSets() { return exerciseSets; }
    public void setExerciseSets(List<ExerciseSet> exerciseSets) { this.exerciseSets = exerciseSets; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Point {
        private Double lat;
        private Double lon;
        public Double getLat() { return lat; }
        public void setLat(Double lat) { this.lat = lat; }
        public Double getLon() { return lon; }
        public void setLon(Double lon) { this.lon = lon; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Split {
        private Double distance;
        private Double duration;
        private Double elevationGain;
        private Double averageSpeed;
        private Double averageHR;
        public Double getDistance() { return distance; }
        public void setDistance(Double distance) { this.distance = distance; }
        public Double getDuration() { return duration; }
        public void setDuration(Double duration) { this.duration = duration; }
        public Double getElevationGain() { return elevationGain; }
        public void setElevationGain(Double elevationGain) { this.elevationGain = elevationGain; }
        public Double getAverageSpeed() { return averageSpeed; }
        public void setAverageSpeed(Double averageSpeed) { this.averageSpeed = averageSpeed; }
        public Double getAverageHR() { return averageHR; }
        public void setAverageHR(Double averageHR) { this.averageHR = averageHR; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExerciseSet {
        private String name;
        private Integer reps;
        private Double weightKg;
        private Double duration;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getReps() { return reps; }
        public void setReps(Integer reps) { this.reps = reps; }
        public Double getWeightKg() { return weightKg; }
        public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }
        public Double getDuration() { return duration; }
        public void setDuration(Double duration) { this.duration = duration; }
    }
}
