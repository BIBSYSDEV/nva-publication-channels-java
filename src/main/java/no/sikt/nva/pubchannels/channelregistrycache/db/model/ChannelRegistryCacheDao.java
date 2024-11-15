package no.sikt.nva.pubchannels.channelregistrycache.db.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import no.sikt.nva.pubchannels.channelregistrycache.LevelForYear;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbImmutable(builder = ChannelRegistryCacheDao.Builder.class)
public record ChannelRegistryCacheDao(UUID identifier,
                                      String type, String title,
                                      String printIssn, String onlineIssn, String isbn, String ceased,
                                      List<LevelForYear> levelHistory, URI uri) {

    public static final String PRIMARY_KEY = "PK0";
    public static final String SORT_KEY = "SK0";

    @DynamoDbPartitionKey
    @DynamoDbAttribute(PRIMARY_KEY)
    public String primaryKeyHashKey() {
        return identifier.toString();
    }

    @DynamoDbSortKey
    @DynamoDbAttribute(SORT_KEY)
    public String primaryKeyRangeKey() {
        return identifier.toString();
    }

    public static Builder builder() {
        return new ChannelRegistryCacheDao.Builder();
    }


    public static final class Builder {

        private UUID identifier;
        private String type;
        private String title;
        private String printIssn;
        private String onlineIssn;
        private String isbn;
        private String ceased;
        private List<LevelForYear> levelHistory;
        private URI uri;

        private Builder() {
        }

        public Builder identifier(UUID identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder printIssn(String printIssn) {
            this.printIssn = printIssn;
            return this;
        }

        public Builder onlineIssn(String onlineIssn) {
            this.onlineIssn = onlineIssn;
            return this;
        }

        public Builder isbn(String isbn) {
            this.isbn = isbn;
            return this;
        }

        public Builder ceased(String ceased) {
            this.ceased = ceased;
            return this;
        }

        public Builder levelHistory(List<LevelForYear> levelHistory) {
            this.levelHistory = levelHistory;
            return this;
        }

        public Builder uri(URI uri) {
            this.uri = uri;
            return this;
        }

        public Builder primaryKeyHashKey(String noop) {
            // Used by @DynamoDbImmutable for building the object
            return this;
        }

        public Builder primaryKeyRangeKey(String noop) {
            // Used by @DynamoDbImmutable for building the object
            return this;
        }

        public ChannelRegistryCacheDao build() {
            return new ChannelRegistryCacheDao(identifier, type, title, printIssn, onlineIssn, isbn, ceased,
                                               levelHistory, uri);
        }
    }
}
