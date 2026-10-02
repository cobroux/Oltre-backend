package io.oltre_backend.sport;

public class GarminRecordsResponseDTO {

    private GarminRecordsDTO allTime;
    private GarminRecordsDTO thisYear;

    public GarminRecordsResponseDTO(GarminRecordsDTO allTime, GarminRecordsDTO thisYear) {
        this.allTime = allTime;
        this.thisYear = thisYear;
    }

    public GarminRecordsDTO getAllTime() {
        return allTime;
    }

    public GarminRecordsDTO getThisYear() {
        return thisYear;
    }
}
