package no.sikt.nva.pubchannels.channelregistrycache;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import java.io.StringReader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

public final class ChannelRegistryCsvLoader {

  private static final int HEADER_POSITION = 1;
  private static final int MAX_LOG_LENGTH = 150;
  private final S3Client s3Client;

  public ChannelRegistryCsvLoader(S3Client s3Client) {
    this.s3Client = s3Client;
  }

  public LoadResult getEntries() {
    var value = s3Client.getObject(getCacheRequest(), ResponseTransformer.toBytes()).asUtf8String();
    return parseCsv(value);
  }

  public record LoadResult(Stream<ChannelRegistryCacheEntry> entries, Supplier<String> report) {}

  private static GetObjectRequest getCacheRequest() {
    return GetObjectRequest.builder()
        .bucket(ChannelRegistryCacheConfig.CACHE_BUCKET)
        .key(ChannelRegistryCacheConfig.CHANNEL_REGISTER_CACHE_S3_OBJECT)
        .build();
  }

  private LoadResult parseCsv(String value) {
    var lines = value.lines().toList();
    if (lines.isEmpty()) {
      return new LoadResult(Stream.of(), () -> "No data");
    }

    var header = lines.getFirst();
    var failures = new ConcurrentHashMap<Integer, FailureInfo>();
    var totalLines = lines.size() - HEADER_POSITION;

    var stream =
        IntStream.range(1, lines.size())
            .parallel()
            .mapToObj(i -> processLine(Map.entry(i, lines.get(i).trim()), header, failures))
            .filter(java.util.Objects::nonNull);

    Supplier<String> reportSupplier = () -> generateReport(failures, totalLines);

    return new LoadResult(stream, reportSupplier);
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

  private static String generateReport(Map<Integer, FailureInfo> failures, int totalLines) {
    if (!failures.isEmpty()) {
      return failures.entrySet().stream()
          .map(
              entry ->
                  "Line %d: %s | Content: %s%n"
                      .formatted(
                          entry.getKey(),
                          entry.getValue().errorMessage(),
                          truncate(entry.getValue().line(), MAX_LOG_LENGTH)))
          .collect(
              Collectors.joining(
                  "%n".formatted(),
                  "",
                  "%n%nFailed to parse %d out of %d CSV lines"
                      .formatted(failures.size(), totalLines)
                  ));
    } else {
      return "Successfully parsed all %s CSV lines".formatted(totalLines);
    }
  }

  public static String truncate(String content, int maxLength) {
    if (content == null || content.length() <= maxLength) {
      return content;
    }
    return content.substring(0, maxLength) + "...";
  }

  private record FailureInfo(String errorMessage, String line) {}
}
