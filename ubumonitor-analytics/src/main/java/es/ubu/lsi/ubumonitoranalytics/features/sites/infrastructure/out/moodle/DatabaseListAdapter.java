package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.out.moodle;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.DatabaseListPort;
import es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model.DatabaseHostInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model.DatabaseInfo;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.DatabaseConnectionException;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle.config.MoodleConfig;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DatabaseListAdapter implements DatabaseListPort {

  private static final String DB_EXTENSION = ".mv.db";
  private static final Pattern DB_FILE_PATTERN =
      Pattern.compile("^([^~]+)~([^~]+)~(.+)\\.mv\\.db$");

  private final MoodleConfig moodleConfig;

  @Override
  public List<DatabaseHostInfo> getDatabaseList() {
    Path dir = Path.of(moodleConfig.getDb().getBasePath());

    try (Stream<Path> files = Files.list(dir)) {
      Map<URI, List<DatabaseInfo>> databasesByHost =
          files
              .filter(Files::isRegularFile)
              .sorted(Comparator.comparing(path -> path.getFileName().toString()))
              .map(this::toDatabaseInfo)
              .flatMap(Optional::stream)
              .collect(
                  Collectors.groupingBy(
                      DatabaseInfo::getHost, LinkedHashMap::new, Collectors.toList()));

      return databasesByHost.entrySet().stream().map(this::toHostInfo).toList();
    } catch (IOException e) {
      throw new DatabaseConnectionException("Error reading database files", e);
    }
  }

  private Optional<DatabaseInfo> toDatabaseInfo(Path path) {
    String fileName = path.getFileName().toString();
    Matcher matcher = DB_FILE_PATTERN.matcher(fileName);
    if (!matcher.matches()) {
      return Optional.empty();
    }

    try {
      String scheme = matcher.group(1);
      String hostName = matcher.group(2);
      String userName = matcher.group(3);

      return Optional.of(
          DatabaseInfo.builder()
              .id(fileName.substring(0, fileName.length() - DB_EXTENSION.length()))
              .userName(userName)
              .host(URI.create(scheme + "://" + hostName))
              .databaseFile(path.toAbsolutePath().normalize().toUri())
              .build());
    } catch (RuntimeException e) {
      log.error("Error processing database file {}", path, e);
      return Optional.empty();
    }
  }

  private DatabaseHostInfo toHostInfo(Map.Entry<URI, List<DatabaseInfo>> entry) {
    return DatabaseHostInfo.builder().host(entry.getKey()).databases(entry.getValue()).build();
  }
}
