package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database;

import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle.config.MoodleConfig;
import es.ubu.lsi.ubumonitoranalytics.util.DatabaseUtil;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantDatabaseInitializer {

  private final MoodleConfig moodleConfig;

  public void createIfNotExists(URI host, String username, String dbPassword) {

    DatabaseUtil.createAndInitializeDatabase(
        moodleConfig.getDb().getJdbcUrlTemplate(),
        moodleConfig.getDb().getBasePath(),
        host,
        username,
        dbPassword);
  }
}
