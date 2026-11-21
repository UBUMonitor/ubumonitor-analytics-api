package es.ubu.lsi.ubumonitoranalytics.features.system.database.infrastructure.out;

import es.ubu.lsi.ubumonitoranalytics.features.system.database.domain.model.DatabaseSnapshot;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.BadRequestException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DatabaseSnapshotFileNaming {

  private static final DateTimeFormatter FILE_TIMESTAMP_FORMATTER =
      DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmssSSS'Z'", Locale.ROOT)
          .withZone(ZoneOffset.UTC)
          .withResolverStyle(ResolverStyle.STRICT);
  private static final Pattern SNAPSHOT_FILE_PATTERN =
      Pattern.compile("^(.+)~(\\d{8}T\\d{9}Z)(?:~([^~]+))?\\.db$");

  private DatabaseSnapshotFileNaming() {}

  static String fileName(String databaseId, Instant createdAt, String name) {
    if (name != null) {
      String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
      if (encodedName.length() > 100) {
        throw new BadRequestException("The encoded snapshot name must not exceed 100 characters");
      }
      String baseFileName = fileName(databaseId, createdAt);
      return baseFileName.substring(0, baseFileName.length() - ".db".length())
          + "~"
          + encodedName
          + ".db";
    }
    return fileName(databaseId, createdAt);
  }

  private static String fileName(String databaseId, Instant createdAt) {
    return databaseId + "~" + FILE_TIMESTAMP_FORMATTER.format(createdAt) + ".db";
  }

  static Optional<DatabaseSnapshot> fromPath(Path snapshotFile) {
    if (snapshotFile == null || snapshotFile.getFileName() == null) {
      return Optional.empty();
    }

    String fileName = snapshotFile.getFileName().toString();
    Matcher matcher = SNAPSHOT_FILE_PATTERN.matcher(fileName);
    if (!matcher.matches()) {
      return Optional.empty();
    }

    String databaseId = matcher.group(1);
    String timestamp = matcher.group(2);
    String encodedName = matcher.group(3);
    try {
      Instant createdAt = Instant.from(FILE_TIMESTAMP_FORMATTER.parse(timestamp));
      String snapshotId = fileName.substring(0, fileName.length() - ".db".length());
      String name = null;
      if (encodedName != null) {
        name = URLDecoder.decode(encodedName, StandardCharsets.UTF_8);
      }
      return Optional.of(
          DatabaseSnapshot.builder()
              .id(snapshotId)
              .databaseId(databaseId)
              .name(name)
              .createdAt(createdAt)
              .snapshotFile(snapshotFile.toAbsolutePath().normalize().toUri())
              .build());
    } catch (DateTimeParseException | IllegalArgumentException exception) {
      return Optional.empty();
    }
  }
}
