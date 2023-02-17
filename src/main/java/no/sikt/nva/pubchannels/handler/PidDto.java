package no.sikt.nva.pubchannels.handler;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import nva.commons.core.JacocoGenerated;
import nva.commons.core.paths.UriWrapper;

import java.net.URI;
import java.util.Objects;

public class PidDto {
    private static final String PID = "pid";
    private static final String ID = "id";

    @JsonProperty(ID)
    private final URI id;
    @JsonProperty(PID)
    private final String pid;

    @JsonCreator
    public PidDto(@JsonProperty(ID) URI id, @JsonProperty(PID) String pid) {
        this.id = id;
        this.pid = pid;
    }


    public static PidDto create(URI selfUriBase, String pid) {
        var id = UriWrapper.fromUri(selfUriBase)
                .addChild(pid)
                .getUri();
        return new PidDto(id, pid);
    }

    @JacocoGenerated
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PidDto pidDto = (PidDto) o;
        return Objects.equals(pid, pidDto.pid);
    }

    @Override
    @JacocoGenerated
    public int hashCode() {
        return Objects.hash(pid);
    }

    public URI getId() {
        return id;
    }

    public String getPid() {
        return pid;
    }
}
