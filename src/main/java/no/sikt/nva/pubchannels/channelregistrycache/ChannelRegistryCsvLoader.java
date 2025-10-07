package no.sikt.nva.pubchannels.channelregistrycache;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import java.io.StringReader;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

public final class ChannelRegistryCsvLoader {

  private static final int HEADER_POSITION = 1;
  private String report;
  private final S3Client s3Client;

  public ChannelRegistryCsvLoader(S3Client s3Client) {
    this.s3Client = s3Client;
  }

  public Stream<ChannelRegistryCacheEntry> getEntries() {
    var value = s3Client.getObject(getCacheRequest(), ResponseTransformer.toBytes()).asUtf8String();
    return parseCsv(value);
  }

  public String getReport() {
    return report;
  }

  private static GetObjectRequest getCacheRequest() {
    return GetObjectRequest.builder()
        .bucket(ChannelRegistryCacheConfig.CACHE_BUCKET)
        .key(ChannelRegistryCacheConfig.CHANNEL_REGISTER_CACHE_S3_OBJECT)
        .build();
  }

  private Stream<ChannelRegistryCacheEntry> parseCsv(String value) {
    var lines = value.lines().toList();
    if (lines.isEmpty()) {
      return Stream.of();
    }

    var header = lines.getFirst();
    var failures = new ConcurrentHashMap<Integer, FailureInfo>();

    return IntStream.range(1, lines.size())
        .parallel()
        .mapToObj(i -> processLine(Map.entry(i, lines.get(i).trim()), header, failures))
        .filter(java.util.Objects::nonNull)
        .collect(
            Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                  this.report = generateReport(failures, lines.size() - HEADER_POSITION);
                  return list.stream(); // Re-stream for downstream
                }));
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
                  "  Line %d: %s | Content: %s"
                      .formatted(
                          entry.getKey(), entry.getValue().errorMessage(), entry.getValue().line()))
          .collect(
              Collectors.joining(
                  "%n".formatted(),
                  "Failed to parse %d out of %d CSV lines:%n"
                      .formatted(failures.size(), totalLines), // prefix
                  "" // suffix
                  ));
    } else {
      return "Successfully parsed all %s CSV lines".formatted(totalLines);
    }
  }

  private record FailureInfo(String errorMessage, String line) {}
}
