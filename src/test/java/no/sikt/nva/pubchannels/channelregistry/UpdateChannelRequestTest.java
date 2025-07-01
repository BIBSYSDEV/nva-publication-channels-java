package no.sikt.nva.pubchannels.channelregistry;

import static no.unit.nva.testutils.RandomDataGenerator.randomIsbn10;
import static no.unit.nva.testutils.RandomDataGenerator.randomIssn;
import static no.unit.nva.testutils.RandomDataGenerator.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistry.ChannelRegistryUpdateChannelRequest.Fields;
import org.junit.jupiter.api.Test;

class UpdateChannelRequestTest {

  @Test
  void shouldConvertUpdatePublisherRequestToChannelRegistryRequestCorrectly() {
    var isbn = randomIsbn10();
    var name = randomString();
    var identifier = UUID.randomUUID().toString().toUpperCase();
    var request = new UpdatePublisherRequest(name, isbn);

    var channelRegistryRequest = request.toChannelRegistryUpdateRequest(identifier);

    var expected =
        new ChannelRegistryUpdateChannelRequest(
            new Fields(identifier, name, null, null, isbn), "publisher");

    assertEquals(expected, channelRegistryRequest);
  }

  @Test
  void shouldConvertUpdateSerialPublicationRequestToChannelRegistryRequestCorrectly() {
    var printIssn = randomIssn();
    var onlineIssn = randomIssn();
    var name = randomString();
    var identifier = UUID.randomUUID().toString().toUpperCase();
    var request = new UpdateSerialPublicationRequest(name, printIssn, onlineIssn);

    var channelRegistryRequest = request.toChannelRegistryUpdateRequest(identifier);

    var expected =
        new ChannelRegistryUpdateChannelRequest(
            new Fields(identifier, name, printIssn, onlineIssn, null), "serial-publication");

    assertEquals(expected, channelRegistryRequest);
  }
}
