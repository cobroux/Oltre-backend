package io.oltre_backend.coach;

public class PlanDayDTO {

    private String day;
    private String type;
    private String label;
    private Double distanceKm;
    private boolean done;

    public PlanDayDTO() {}

    public PlanDayDTO(String day, String type, String label, Double distanceKm, boolean done) {
        this.day = day;
        this.type = type;
        this.label = label;
        this.distanceKm = distanceKm;
        this.done = done;
    }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }
}
