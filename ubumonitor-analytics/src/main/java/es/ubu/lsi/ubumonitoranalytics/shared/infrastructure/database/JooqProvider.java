package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database;

import com.zaxxer.hikari.HikariDataSource;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.database.TenantDatabasePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.DatabaseCreationException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle.config.MoodleConfig;
import es.ubu.lsi.ubumonitoranalytics.util.DatabaseUtil;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
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

/** Caches tenant data sources and creates a jOOQ context for each access. */
@Slf4j
@Component
public class JooqProvider implements TenantDatabasePort {

  private final MoodleConfig moodleConfig;

  // Cache DataSource instances only; DSLContext instances are request-scoped.
  private final ConcurrentHashMap<String, DataSource> dataSourceCache;

  private final Set<String> migratedTenants = ConcurrentHashMap.newKeySet();

  public JooqProvider(MoodleConfig moodleConfig) {

    this.moodleConfig = moodleConfig;

    this.dataSourceCache = new ConcurrentHashMap<>();
  }

  /** Returns the cached tenant data source, creating and migrating it when necessary. */
  public DataSource getDataSource(SessionData session) {

    if (session == null) {
      throw new IllegalStateException("No session available");
    }

    String tenantId = TenantContext.buildTenantId(session);
    return dataSourceCache.computeIfAbsent(tenantId, id -> createTenantDataSource(session));
  }

  /** Creates a jOOQ context backed by the data source for the current tenant. */
  public DSLContext getDSLContext(SessionData sessionData) {

    DataSource ds = getDataSource(sessionData);

    Configuration config = new DefaultConfiguration().set(ds).set(SQLDialect.H2);

    return new DefaultDSLContext(config);
  }

  /** Creates and configures a tenant data source, including its Flyway schema migration. */
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

  /** Runs the Flyway migration once for the supplied tenant. */
  private void migrateIfNeeded(String tenantId, DataSource ds) throws Exception {

    if (migratedTenants.contains(tenantId)) {
      return;
    }

    String migrationPath = extractMigrationsToTemp();
    try {
      log.info("Running Flyway migration for tenant {}", tenantId);
      Flyway.configure()
          .dataSource(ds)
          .locations("filesystem:" + migrationPath)
          .baselineOnMigrate(true)
          .validateOnMigrate(true)
          .load()
          .migrate();

      migratedTenants.add(tenantId);
      log.info("Migration completed for tenant {}", tenantId);
    } finally {
      deleteDirectory(Path.of(migrationPath));
    }
  }

  /** Closes and removes the data source associated with a tenant session. */
  @Override
  public void closeTenant(SessionData session) {

    if (session == null) return;

    String tenantId = TenantContext.buildTenantId(session);

    DataSource ds = dataSourceCache.get(tenantId);

    if (ds instanceof HikariDataSource hikari) {
      hikari.close();
    }

    dataSourceCache.remove(tenantId);
    migratedTenants.remove(tenantId);

    log.info("Closed tenant {}", tenantId);
  }

  /** Closes all cached tenant data sources and clears the provider state. */
  public void clearAll() {
    for (DataSource dataSource : dataSourceCache.values()) {
      if (dataSource instanceof HikariDataSource hikari) {
        hikari.close();
      }
    }
    dataSourceCache.clear();
    migratedTenants.clear();
    log.info("Cleared all tenants");
  }

  private static void deleteDirectory(Path directory) throws IOException {
    if (!Files.exists(directory)) {
      return;
    }

    try (Stream<Path> paths = Files.walk(directory)) {
      List<Path> pathsToDelete = paths.sorted(Comparator.reverseOrder()).toList();
      for (Path path : pathsToDelete) {
        Files.deleteIfExists(path);
      }
    }
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
