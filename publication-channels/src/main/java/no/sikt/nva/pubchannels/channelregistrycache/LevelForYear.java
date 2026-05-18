package no.sikt.nva.pubchannels.channelregistrycache;

import nva.commons.core.JacocoGenerated;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;

@DynamoDbImmutable(builder = LevelForYear.Builder.class)
public record LevelForYear(
    @DynamoDbAttribute("year") String year, @DynamoDbAttribute("level") String level) {

  @JacocoGenerated
  public static Builder builder() {
    return new Builder();
  }

  @JacocoGenerated
  public static final class Builder {

    private String year;
    private String level;

    private Builder() {}

    public Builder year(String year) {
      this.year = year;
      return this;
    }

    public Builder level(String level) {
      this.level = level;
      return this;
    }

    public LevelForYear build() {
      return new LevelForYear(year, level);
    }
  }
}
