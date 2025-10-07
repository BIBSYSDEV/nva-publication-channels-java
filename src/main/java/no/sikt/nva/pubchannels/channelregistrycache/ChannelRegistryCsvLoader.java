package no.sikt.nva.pubchannels.channelregistrycache;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

public final class ChannelRegistryCsvLoader {

  private static final Logger logger = LoggerFactory.getLogger(ChannelRegistryCsvLoader.class);
  private static final int HEADER_POSITION = 1;
  private final List<ChannelRegistryCacheEntry> cacheEntries;

  private ChannelRegistryCsvLoader(List<ChannelRegistryCacheEntry> cacheEntries) {
    this.cacheEntries = cacheEntries;
  }

  public static ChannelRegistryCsvLoader load(S3Client s3Client) {
    var value = s3Client.getObject(getCacheRequest(), ResponseTransformer.toBytes()).asUtf8String();
    return new ChannelRegistryCsvLoader(getChannelRegistryCacheFromString(value));
  }

  public List<ChannelRegistryCacheEntry> getCacheEntries() {
    return cacheEntries;
  }

  private static GetObjectRequest getCacheRequest() {
    return GetObjectRequest.builder()
        .bucket(ChannelRegistryCacheConfig.CACHE_BUCKET)
        .key(ChannelRegistryCacheConfig.CHANNEL_REGISTER_CACHE_S3_OBJECT)
        .build();
  }

  private static List<ChannelRegistryCacheEntry> getChannelRegistryCacheFromString(String value) {
    var lines = value.lines().toList();
    if (lines.isEmpty()) {
      return List.of();
    }

    var header = lines.getFirst();
    var failures = new ConcurrentHashMap<Integer, FailureInfo>();

    // Process lines in parallel
    var result =
        java.util.stream.IntStream.range(1, lines.size())
            .parallel()
            .mapToObj(
                counter ->
                    extractEntity(Map.entry(counter, lines.get(counter).trim()), header, failures))
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toCollection(ArrayList::new));

    if (!failures.isEmpty()) {
      if (logger.isWarnEnabled()) {
        logger.warn(createReport(failures, lines.size() - HEADER_POSITION));
      }
    } else {
      logger.info("Successfully parsed all {} CSV lines", lines.size() - HEADER_POSITION);
    }

    return result;
  }

  private static String createReport(Map<Integer, FailureInfo> failures, int totalLines) {
    return failures.entrySet().stream()
        .map(
            entry ->
                String.format(
                    "  Line %d: %s | Content: %s%n",
                    entry.getKey(), entry.getValue().errorMessage(), entry.getValue().line()))
        .collect(
            Collectors.joining(
                "",
                String.format(
                    "Failed to parse %d out of %d CSV lines:%n", failures.size(), totalLines),
                ""));
  }

  private static ChannelRegistryCacheEntry extractEntity(
      Entry<Integer, String> entry, String header, Map<Integer, FailureInfo> failures) {
    try {
      var csvData = header + "\n" + entry.getValue();

      var entries =
          new CsvToBeanBuilder<ChannelRegistryCacheEntry>(new StringReader(csvData))
              .withType(ChannelRegistryCacheEntry.class)
              .withSeparator(';')
              .withIgnoreEmptyLine(true)
              .withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
              .build()
              .parse();

      return entries.isEmpty() ? null : entries.getFirst();
    } catch (Exception e) {
      var rootCause = (Exception) e.getCause();
      var errorMessage = rootCause.getClass().getSimpleName() + ": " + rootCause.getMessage();
      failures.put(entry.getKey(), new FailureInfo(errorMessage, entry.getValue()));
      return null;
    }
  }

  private record FailureInfo(String errorMessage, String line) {}
}
