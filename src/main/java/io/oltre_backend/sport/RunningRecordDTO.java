package io.oltre_backend.sport;

public class RunningRecordDTO {

    private String label;
    private double targetDistanceKm;
    private double distanceKm;
    private int durationSeconds;
    private String date;

    public RunningRecordDTO(String label, double targetDistanceKm, double distanceKm,
                             int durationSeconds, String date) {
        this.label = label;
        this.targetDistanceKm = targetDistanceKm;
        this.distanceKm = distanceKm;
        this.durationSeconds = durationSeconds;
        this.date = date;
    }

    public String getLabel() {
        return label;
    }

    public double getTargetDistanceKm() {
        return targetDistanceKm;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public String getDate() {
        return date;
    }
}
