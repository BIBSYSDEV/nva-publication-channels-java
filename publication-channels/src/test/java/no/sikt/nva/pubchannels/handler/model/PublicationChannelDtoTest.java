package no.sikt.nva.pubchannels.handler.model;

import static no.unit.nva.testutils.RandomDataGenerator.randomUri;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistryPublisher;
import no.sikt.nva.pubchannels.channelregistry.model.ChannelRegistrySerialPublication;
import org.junit.jupiter.api.Test;

class PublicationChannelDtoTest {

  @Test
  void shouldReturnCurrentYearWhenMissingInRequestAndInThirdPartyChannelWhenPublisher() {
    var dto = PublisherDto.create(randomUri(), randomPublisher(), null);

    assertEquals(String.valueOf(LocalDate.now().getYear()), dto.year());
  }

  @Test
  void shouldReturnCurrentYearWhenMissingInRequestAndInThirdPartyChannelWhenSerialPublication() {
    var dto = SerialPublicationDto.create(randomUri(), randomSerialPublication(), null);

    assertEquals(String.valueOf(LocalDate.now().getYear()), dto.year());
  }

  private static ChannelRegistryPublisher randomPublisher() {
    return new ChannelRegistryPublisher(
        UUID.randomUUID().toString(), null, null, null, randomUri(), null, null);
  }

  private static ChannelRegistrySerialPublication randomSerialPublication() {
    return new ChannelRegistrySerialPublication(
        UUID.randomUUID().toString(), null, null, null, null, randomUri(), null, "journal");
  }
}
