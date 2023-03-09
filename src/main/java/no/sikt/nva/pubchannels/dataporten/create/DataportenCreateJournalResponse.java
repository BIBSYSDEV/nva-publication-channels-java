package no.sikt.nva.pubchannels.dataporten.create;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DataportenCreateJournalResponse {
    private static final String PID_FIELD = "pid";
    @JsonProperty(PID_FIELD)
    private final String pid;

    @JsonCreator
    public DataportenCreateJournalResponse(@JsonProperty(PID_FIELD) String pid) {
        this.pid = pid;
    }

    public String getPid() {
        return pid;
    }
}