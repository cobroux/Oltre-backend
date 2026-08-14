package io.oltre_backend.sport;

public class StravaActivityDTO {

    private String id;
    private String name;
    private String description;
    private String sportType;
    private String startLocal;
    private Double distance;
    private Integer movingTime;
    private Double elevationGain;
    private Double avgSpeed;
    private Double maxSpeed;
    private Integer calories;
    private Integer relativeEffort;
    private Integer kudosCount;
    private Integer achievementCount;
    private Integer prCount;

    public StravaActivityDTO() {}

    public StravaActivityDTO(StravaActivity a) {
        this.id               = String.valueOf(a.getId());
        this.name             = a.getName();
        this.description      = a.getDescription();
        this.sportType        = a.getSportType();
        this.startLocal       = a.getStartLocal();
        this.distance         = a.getDistance();
        this.movingTime       = a.getMovingTime();
        this.elevationGain    = a.getElevationGain();
        this.avgSpeed         = a.getAvgSpeed();
        this.maxSpeed         = a.getMaxSpeed();
        this.calories         = a.getCalories() != null ? a.getCalories().intValue() : 0;
        this.relativeEffort   = a.getRelativeEffort();
        this.kudosCount       = a.getKudosCount();
        this.achievementCount = a.getAchievementCount();
        this.prCount          = a.getPrCount();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getSportType() { return sportType; }
    public String getStartLocal() { return startLocal; }
    public Double getDistance() { return distance; }
    public Integer getMovingTime() { return movingTime; }
    public Double getElevationGain() { return elevationGain; }
    public Double getAvgSpeed() { return avgSpeed; }
    public Double getMaxSpeed() { return maxSpeed; }
    public Integer getCalories() { return calories; }
    public Integer getRelativeEffort() { return relativeEffort; }
    public Integer getKudosCount() { return kudosCount; }
    public Integer getAchievementCount() { return achievementCount; }
    public Integer getPrCount() { return prCount; }
}
