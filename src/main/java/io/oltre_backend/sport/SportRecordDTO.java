package io.oltre_backend.sport;

public class SportRecordDTO {

    private String sportType;
    private Double bestDistanceKm;
    private String bestDistanceDate;
    private Double bestElevationGainM;
    private String bestElevationDate;
    private Double bestAvgSpeedKmh;
    private String bestAvgSpeedDate;

    public SportRecordDTO(String sportType) {
        this.sportType = sportType;
    }

    public String getSportType() {
        return sportType;
    }

    public Double getBestDistanceKm() {
        return bestDistanceKm;
    }

    public void setBestDistanceKm(Double bestDistanceKm) {
        this.bestDistanceKm = bestDistanceKm;
    }

    public String getBestDistanceDate() {
        return bestDistanceDate;
    }

    public void setBestDistanceDate(String bestDistanceDate) {
        this.bestDistanceDate = bestDistanceDate;
    }

    public Double getBestElevationGainM() {
        return bestElevationGainM;
    }

    public void setBestElevationGainM(Double bestElevationGainM) {
        this.bestElevationGainM = bestElevationGainM;
    }

    public String getBestElevationDate() {
        return bestElevationDate;
    }

    public void setBestElevationDate(String bestElevationDate) {
        this.bestElevationDate = bestElevationDate;
    }

    public Double getBestAvgSpeedKmh() {
        return bestAvgSpeedKmh;
    }

    public void setBestAvgSpeedKmh(Double bestAvgSpeedKmh) {
        this.bestAvgSpeedKmh = bestAvgSpeedKmh;
    }

    public String getBestAvgSpeedDate() {
        return bestAvgSpeedDate;
    }

    public void setBestAvgSpeedDate(String bestAvgSpeedDate) {
        this.bestAvgSpeedDate = bestAvgSpeedDate;
    }
}
