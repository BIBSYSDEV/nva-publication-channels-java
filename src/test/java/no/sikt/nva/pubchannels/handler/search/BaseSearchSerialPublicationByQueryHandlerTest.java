package no.sikt.nva.pubchannels.handler.search;

import static java.util.Objects.nonNull;
import static no.sikt.nva.pubchannels.TestConstants.DEFAULT_OFFSET_INT;
import static no.sikt.nva.pubchannels.TestConstants.DEFAULT_SIZE_INT;
import static no.sikt.nva.pubchannels.TestConstants.NAME_QUERY_PARAM;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructPublicationChannelUri;
import static no.sikt.nva.pubchannels.handler.TestUtils.constructRequest;
import static no.unit.nva.testutils.RandomDataGenerator.objectMapper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.net.MediaType;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.sikt.nva.pubchannels.handler.TestChannel;
import no.sikt.nva.pubchannels.handler.model.SerialPublicationDto;
import no.unit.nva.commons.pagination.PaginatedSearchResult;
import nva.commons.apigateway.GatewayResponse;
import nva.commons.apigateway.exceptions.UnprocessableContentException;
import org.junit.jupiter.api.Test;

public abstract class BaseSearchSerialPublicationByQueryHandlerTest
    extends SearchByQueryHandlerTest {

  private static final TypeReference<PaginatedSearchResult<SerialPublicationDto>> TYPE_REF =
      new TypeReference<>() {};

  protected PaginatedSearchResult<SerialPublicationDto> getExpectedSearchResult(
      String year, String queryParamValue, TestChannel testChannel)
      throws UnprocessableContentException {
    var expectedParams = new HashMap<String, String>();
    expectedParams.put("query", queryParamValue);
    if (nonNull(year)) {
      expectedParams.put("year", year);
    }

    var expectedHits = List.of(testChannel.asSerialPublicationDto(selfBaseUri, year));
    return PaginatedSearchResult.create(
        constructPublicationChannelUri(testChannel.type(), expectedParams),
        DEFAULT_OFFSET_INT,
        DEFAULT_SIZE_INT,
        expectedHits.size(),
        expectedHits);
  }

  @Override
  protected PaginatedSearchResult<?> getActualSearchResult(GatewayResponse<?> response)
      throws JsonProcessingException {
    return objectMapper.readValue(response.getBody(), TYPE_REF);
  }

  @Test
  protected void shouldReturnResultWithYearWhenQueryOmitsYear() throws IOException {
    var testChannel = new TestChannel(year, pid, type).withName(name);
    mockChannelRegistryResponse(
        null, NAME_QUERY_PARAM, name, List.of(testChannel.asChannelRegistryResponseBody()));

    var input = constructRequest(Map.of("query", name), MediaType.ANY_TYPE);
    handlerUnderTest.handleRequest(input, output, context);

    var response = GatewayResponse.fromOutputStream(output, PaginatedSearchResult.class);
    var result = objectMapper.readValue(response.getBody(), TYPE_REF).getHits().getFirst();

    assertThat(result.year(), is(equalTo(year)));
  }
}
