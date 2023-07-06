package no.sikt.nva.pubchannels.dataporten.model.create;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DataportenCreateSeriesResponse {
    private static final String PID_FIELD = "pid";
    @JsonProperty(PID_FIELD)
    private final String pid;

    @JsonCreator
    public DataportenCreateSeriesResponse(@JsonProperty(PID_FIELD) String pid) {
        this.pid = pid;
    }

    public String getPid() {
        return pid;
    }
}
