package es.ubu.lsi.ubumonitoranalytics.features.system.database.infrastructure.out;

import es.ubu.lsi.ubumonitoranalytics.features.system.database.domain.model.DatabaseSnapshot;
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
      Pattern.compile("^(.+)~(\\d{8}T\\d{9}Z)(?:~([A-Za-z0-9 _.-]+))?\\.db$");

  private DatabaseSnapshotFileNaming() {}

  static String fileName(String databaseId, Instant createdAt, String name) {
    String snapshotFileName = databaseId + "~" + FILE_TIMESTAMP_FORMATTER.format(createdAt);
    if (name != null) {
      snapshotFileName += "~" + name;
    }
    return snapshotFileName + ".db";
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
    String name = matcher.group(3);
    try {
      Instant createdAt = Instant.from(FILE_TIMESTAMP_FORMATTER.parse(timestamp));
      String snapshotId = fileName.substring(0, fileName.length() - ".db".length());
      return Optional.of(
          DatabaseSnapshot.builder()
              .id(snapshotId)
              .databaseId(databaseId)
              .name(name)
              .createdAt(createdAt)
              .snapshotFile(snapshotFile.toAbsolutePath().normalize().toUri())
              .build());
    } catch (DateTimeParseException exception) {
      return Optional.empty();
    }
  }
}
