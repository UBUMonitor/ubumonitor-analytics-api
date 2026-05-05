package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zaxxer.hikari.HikariDataSource;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.DatabaseCreationException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle.config.MoodleConfig;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import es.ubu.lsi.ubumonitoranalytics.util.DatabaseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider<String> {

    private final transient CurrentSessionContext currentSessionContext;
    private final transient MoodleConfig moodleConfig;

    private final transient Cache<String, DataSource> cache;

    private final Set<String> migratedTenants = ConcurrentHashMap.newKeySet();

    private final transient DataSource bootstrapDataSource;

    public MultiTenantConnectionProviderImpl(CurrentSessionContext currentSessionContext, MoodleConfig moodleConfig) {
        this.currentSessionContext = currentSessionContext;
        this.moodleConfig = moodleConfig;
        this.bootstrapDataSource = createBootstrap();

        this.cache = Caffeine.newBuilder()
            .maximumSize(100) // máximo tenants activos
            .expireAfterAccess(24, TimeUnit.HOURS) // TTL
            .removalListener((key, ds, cause) -> closeDataSource((DataSource) ds))
            .build();
    }

    // =========================
    // Hibernate hooks
    // =========================

    @Override
    public Connection getAnyConnection() throws SQLException {
        return bootstrapDataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantId) throws SQLException {

        DataSource ds = cache.get(tenantId, this::createTenantDataSource);
        return ds.getConnection();
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection)
        throws SQLException {

        connection.close();
    }

    // =========================
    // CREACIÓN DE TENANT
    // =========================

    private DataSource createTenantDataSource(String tenantId) {

        SessionData session = currentSessionContext.getSessionData();

        if (session == null) {
            throw new IllegalStateException("No session for tenant " + tenantId);
        }

        try {
            HikariDataSource ds = new HikariDataSource();

            String jdbcUrl = DatabaseUtil.buildJdbcUrl(
                moodleConfig.getDb().getBasePath(),
                session.getHost(),
                session.getUserName()
            );

            ds.setJdbcUrl(jdbcUrl);
            ds.setUsername("sa");
            ds.setPassword(session.getDbPassword() + " " + session.getDbPassword());

            ds.setDriverClassName("org.h2.Driver");

            ds.setMaximumPoolSize(5);
            ds.setMinimumIdle(0);
            ds.setIdleTimeout(600000);     // 10 min
            ds.setMaxLifetime(1800000);    // 30 min
            ds.setConnectionTimeout(30000);

            log.info("Creating tenant DB: {}", tenantId);

            migrateIfNeeded(tenantId, ds);

            return ds;

        } catch (Exception e) {
            throw new DatabaseCreationException("Error creating tenant " + tenantId, e);
        }
    }

    private void migrateIfNeeded(String tenantId, DataSource ds) {

        if (migratedTenants.contains(tenantId)) {
            return;
        }

        synchronized (this) {
            if (migratedTenants.contains(tenantId)) {
                return;
            }

            Flyway flyway = Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .validateOnMigrate(true)
                .load();

            flyway.migrate();

            migratedTenants.add(tenantId);

            log.info("Tenant migrated: {}", tenantId);
        }
    }

    private void closeDataSource(DataSource ds) {
        if (ds instanceof HikariDataSource hikari) {
            try {
                hikari.close();
                log.info("Closed tenant pool");
            } catch (Exception e) {
                log.warn("Error closing datasource: {}", e.getMessage());
            }
        }
    }

    private DataSource createBootstrap() {
        HikariDataSource ds = new HikariDataSource();

        String basePath = moodleConfig.getDb().getBasePath();

        String jdbcUrl = String.format(
            "jdbc:h2:file:%s/bootstrap;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE",
            basePath.replace("\\", "/")
        );

        log.info("BOOTSTRAP DATA SOURCE (FILE): {}", jdbcUrl);

        ds.setJdbcUrl(jdbcUrl);
        ds.setUsername("sa");
        ds.setPassword("");

        ds.setDriverClassName("org.h2.Driver");

        ds.setMaximumPoolSize(1);
        ds.setMinimumIdle(1);
        ds.setConnectionTimeout(30000);

        return ds;
    }

    // =========================
    // REQUIRED
    // =========================

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(@NonNull Class unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(@NonNull Class<T> unwrapType) {
        return null;
    }
}
