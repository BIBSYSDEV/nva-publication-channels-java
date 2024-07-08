package no.sikt.nva.pubchannels;

import com.google.common.net.MediaType;
import java.util.stream.Stream;
import nva.commons.apigateway.MediaTypes;
import org.junit.jupiter.api.Named;

public class TestCommons {

    public static final String LOCALHOST = "localhost";
    public static final String CUSTOM_DOMAIN_BASE_PATH = "publication-channels";
    public static final String NAME_QUERY_PARAM = "name";
    public static final String YEAR_QUERY_PARAM = "year";
    public static final String ISSN_QUERY_PARAM = "issn";
    public static final String PID_QUERY_PARAM = "pid";
    public static final int MAX_LEVEL = 2;
    public static final double MIN_LEVEL = 0;
    public static final String DEFAULT_OFFSET = "0";
    public static final String DEFAULT_SIZE = "10";
    public static final String CHANNEL_REGISTRY_PAGE_NO_PARAM = "pageno";
    public static final String CHANNEL_REGISTRY_PAGE_COUNT_PARAM = "pagecount";
    public static final String WILD_CARD = "*";
    public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    public static final String LOCATION = "Location";

    private TestCommons() {
    }

    public static Stream<Named<MediaType>> mediaTypeProvider() {
        return Stream.of(
            Named.of("JSON UTF-8", MediaType.JSON_UTF_8),
            Named.of("ANY", MediaType.ANY_TYPE),
            Named.of("JSON-LD", MediaTypes.APPLICATION_JSON_LD)
        );
    }
}
