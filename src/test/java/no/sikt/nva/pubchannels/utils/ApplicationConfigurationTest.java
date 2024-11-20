package no.sikt.nva.pubchannels.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import org.junit.jupiter.api.Test;

class ApplicationConfigurationTest {

    @Test
    void some() throws IOException, InterruptedException {
        var client = mock(HttpClient.class);
        when(client.send(any(),eq(BodyHandlers.ofString())))
            .thenReturn(response());
        var configuration = new ApplicationConfiguration(client);


        assertTrue(configuration.shouldUseCache());
    }

    private HttpResponse<String> response() {
        var response = mock(HttpResponse.class);
        when(response.body()).thenReturn(configContent());
        return response;
    }

    private static String configContent() {
        return """
            {
                "publicationChannelCacheEnabled": true
            }
            """;
    }
}