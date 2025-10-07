package no.sikt.nva.pubchannels.channelregistrycache;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
    return new ChannelRegistryCsvLoader(parseCsv(value));
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

  private static List<ChannelRegistryCacheEntry> parseCsv(String value) {
    var lines = value.lines().toList();
    if (lines.isEmpty()) {
      return List.of();
    }

    var header = lines.getFirst();
    var failures = new ConcurrentHashMap<Integer, FailureInfo>();

    var result =
        IntStream.range(1, lines.size())
            .parallel()
            .mapToObj(i -> processLine(Map.entry(i, lines.get(i).trim()), header, failures))
            .filter(java.util.Objects::nonNull)
            .toList();

    logFailures(failures, lines.size() - HEADER_POSITION);
    return result;
  }

  private static ChannelRegistryCacheEntry processLine(
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

  private static void logFailures(Map<Integer, FailureInfo> failures, int totalLines) {
    if (!failures.isEmpty()) {
      var report =
          failures.entrySet().stream()
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
      logger.warn(report);
    } else {
      logger.info("Successfully parsed all {} CSV lines", totalLines);
    }
  }

  private record FailureInfo(String errorMessage, String line) {}
}
