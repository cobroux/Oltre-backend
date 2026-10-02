package io.oltre_backend.sport;

import java.util.Collection;
import java.util.List;

public class GarminRecordsDTO {

    private List<RunningRecordDTO> runningRecords;
    private Collection<SportRecordDTO> otherRecords;

    public GarminRecordsDTO(List<RunningRecordDTO> runningRecords, Collection<SportRecordDTO> otherRecords) {
        this.runningRecords = runningRecords;
        this.otherRecords = otherRecords;
    }

    public List<RunningRecordDTO> getRunningRecords() {
        return runningRecords;
    }

    public Collection<SportRecordDTO> getOtherRecords() {
        return otherRecords;
    }
}
