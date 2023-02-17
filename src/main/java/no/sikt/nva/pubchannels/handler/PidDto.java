package no.sikt.nva.pubchannels.handler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class PidDto {
    private static final String PID = "pid";
    @JsonProperty(PID)
    private final String pid;

    @JsonCreator
    public PidDto(@JsonProperty(PID) String pid) {
        this.pid = pid;
    }

    public String getPid() {
        return pid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PidDto pidDto = (PidDto) o;
        return Objects.equals(pid, pidDto.pid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pid);
    }
}
