package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zaxxer.hikari.HikariDataSource;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.DatabaseCreationException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle.config.MoodleConfig;
import es.ubu.lsi.ubumonitoranalytics.util.DatabaseUtil;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JooqProvider {

  private final MoodleConfig moodleConfig;

  // SOLO cache de DataSource (NO DSLContext)
  private final Cache<String, DataSource> dataSourceCache;

  private final Set<String> migratedTenants = ConcurrentHashMap.newKeySet();

  public JooqProvider(MoodleConfig moodleConfig) {

    this.moodleConfig = moodleConfig;

    this.dataSourceCache = Caffeine.newBuilder().maximumSize(100).build();
  }

  // =========================================================
  // PUBLIC API
  // =========================================================

  public DataSource getDataSource(SessionData session) {

    if (session == null) {
      throw new IllegalStateException("No session available");
    }

    String tenantId = TenantContext.buildTenantId(session);
    return dataSourceCache.get(tenantId, id -> createTenantDataSource(session));
  }

  public DSLContext getDSLContext(SessionData sessionData) {

    DataSource ds = getDataSource(sessionData);

    Configuration config = new DefaultConfiguration().set(ds).set(SQLDialect.H2);

    return new DefaultDSLContext(config);
  }

  // =========================================================
  // DATASOURCE CREATION
  // =========================================================

  private DataSource createTenantDataSource(SessionData session) {

    try {
      HikariDataSource ds = new HikariDataSource();

      String jdbcUrl =
          DatabaseUtil.buildJdbcUrl(
              moodleConfig.getDb().getJdbcUrlTemplate(),
              moodleConfig.getDb().getBasePath(),
              session.getHost(),
              session.getUsername());

      ds.setJdbcUrl(jdbcUrl);
      ds.setUsername("sa");
      ds.setPassword(TenantContext.getPassword(session));
      ds.setDriverClassName("org.h2.Driver");

      ds.setMaximumPoolSize(5);
      ds.setMinimumIdle(0);
      ds.setIdleTimeout(600_000);
      ds.setMaxLifetime(1_800_000);
      ds.setConnectionTimeout(30_000);

      String tenantId = TenantContext.buildTenantId(session);

      log.info("Creating datasource for tenant {}", tenantId);

      migrateIfNeeded(tenantId, ds);

      return ds;

    } catch (Exception e) {
      throw new DatabaseCreationException("Error creating tenant datasource", e);
    }
  }

  // =========================================================
  // FLYWAY (solo una vez por tenant)
  // =========================================================

  private void migrateIfNeeded(String tenantId, DataSource ds) throws Exception {

    if (!migratedTenants.add(tenantId)) {
      return;
    }

    String migrationPath = extractMigrationsToTemp();

    log.info("Running Flyway migration for tenant {}", tenantId);
    Flyway.configure()
        .dataSource(ds)
        .locations("filesystem:" + migrationPath)
        .baselineOnMigrate(true)
        .validateOnMigrate(true)
        .load()
        .migrate();

    log.info("Migration completed for tenant {}", tenantId);
  }

  // =========================================================
  // CLEANUP
  // =========================================================

  public void closeTenant(SessionData session) {

    if (session == null) return;

    String tenantId = TenantContext.buildTenantId(session);

    DataSource ds = dataSourceCache.getIfPresent(tenantId);

    if (ds instanceof HikariDataSource hikari) {
      hikari.close();
    }

    dataSourceCache.invalidate(tenantId);
    migratedTenants.remove(tenantId);

    log.info("Closed tenant {}", tenantId);
  }

  public void clearAll() {
    dataSourceCache.invalidateAll();
    migratedTenants.clear();
    log.info("Cleared all tenants");
  }

  private String extractMigrationsToTemp() throws Exception {

    Path tempDir = Files.createTempDirectory("flyway-migrations");

    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    Resource[] resources = resolver.getResources("classpath:db/migration/*.sql");

    for (Resource resource : resources) {

      Path target = tempDir.resolve(Objects.requireNonNull(resource.getFilename()));

      try (InputStream in = resource.getInputStream()) {
        Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        log.info("Extracted migration {} to {}", resource.getFilename(), target);
      }
    }

    return tempDir.toAbsolutePath().toString();
  }
}
