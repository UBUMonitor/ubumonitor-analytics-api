package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zaxxer.hikari.HikariDataSource;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.DatabaseCreationException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle.config.MoodleConfig;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import es.ubu.lsi.ubumonitoranalytics.util.DatabaseUtil;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.jspecify.annotations.NonNull;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MultiTenantConnectionProviderImpl
    implements MultiTenantConnectionProvider<String> {

    private final transient CurrentSessionContext currentSessionContext;
    private final transient MoodleConfig moodleConfig;

    private final transient Cache<String, DataSource> cache;
    private final Set<String> migratedTenants = ConcurrentHashMap.newKeySet();

    private final transient DataSource bootstrapDataSource;


    public MultiTenantConnectionProviderImpl(
        CurrentSessionContext currentSessionContext,
        MoodleConfig moodleConfig) {

        this.currentSessionContext = currentSessionContext;
        this.moodleConfig = moodleConfig;

        this.bootstrapDataSource = createBootstrap();

        this.cache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(24, TimeUnit.HOURS)
            .build();
    }

    // =========================
    // HIBERNATE HOOKS
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

        SessionData session = currentSessionContext.getSessionData();

        if (session == null) {
            throw new IllegalStateException("No session available");
        }

        String resolvedTenantId = session.getHost() + "_" + session.getUserName();

        DataSource ds = cache.get(resolvedTenantId,
            id -> createTenantDataSource(session)
        );

        Connection connection = ds.getConnection();

        try {
            log.info("   TENANT ACTIVE:");
            log.info("   tenantId   = {}", resolvedTenantId);
            log.info("   host       = {}", session.getHost());
            log.info("   user       = {}", session.getUserName());
            log.info("   jdbcUrl    = {}", connection.getMetaData().getURL());
            log.info("   catalog    = {}", connection.getCatalog());
        } catch (Exception e) {
            log.warn("Error logging datasource info", e);
        }

        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection)
        throws SQLException {
        connection.close();
    }

    // =========================
    // TENANT DATASOURCE
    // =========================

    private DataSource createTenantDataSource(SessionData session) {

        try {
            HikariDataSource ds = new HikariDataSource();

            String jdbcUrl = DatabaseUtil.buildJdbcUrl(
                moodleConfig.getDb().getBasePath(),
                session.getHost(),
                session.getUserName()
            );

            ds.setJdbcUrl(jdbcUrl);
            ds.setUsername("sa");
            ds.setPassword(TenantContext.getPassword(session));

            ds.setDriverClassName("org.h2.Driver");

            ds.setMaximumPoolSize(5);
            ds.setMinimumIdle(0);
            ds.setIdleTimeout(600000);
            ds.setMaxLifetime(1800000);
            ds.setConnectionTimeout(30000);

            String tenantKey = TenantContext.buildTenantId(session);

            log.info("Creating tenant datasource: {}", tenantKey);

            migrateIfNeeded(tenantKey, ds);

            return ds;

        } catch (Exception e) {
            throw new DatabaseCreationException(
                "Error creating tenant datasource", e
            );
        }
    }
    public DataSource getDataSourceForTenant() {

        SessionData session = currentSessionContext.getSessionData();

        if (session == null) {
            throw new IllegalStateException("No session available");
        }

        String resolvedTenantId = TenantContext.buildTenantId(session);

        return cache.get(resolvedTenantId,
            id -> createTenantDataSource(session)
        );
    }
    // =========================
    // MIGRATION
    // =========================

    private void migrateIfNeeded(String tenantId, DataSource ds) {

        if (!migratedTenants.add(tenantId)) {
            return;
        }

        Flyway flyway = Flyway.configure()
            .dataSource(ds)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .validateOnMigrate(true)
            .load();

        flyway.migrate();

        log.info("Tenant migrated: {}", tenantId);
    }

    // =========================
    // BOOTSTRAP
    // =========================

    private DataSource createBootstrap() {

        HikariDataSource ds = new HikariDataSource();

        String jdbcUrl = String.format(
            "jdbc:h2:file:%s/bootstrap;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE;DATABASE_TO_UPPER=false",
            moodleConfig.getDb().getBasePath()
        );

        ds.setJdbcUrl(jdbcUrl);
        ds.setUsername("sa");
        ds.setPassword("");
        ds.setDriverClassName("org.h2.Driver");

        ds.setMaximumPoolSize(1);
        ds.setMinimumIdle(1);

        log.info("BOOTSTRAP DATA SOURCE: {}", jdbcUrl);

        return ds;
    }

    public void closeDataSource(DataSource ds) {
        if (ds instanceof HikariDataSource hikari) {
            hikari.close();
        }
    }

    /**
     * Close and remove the tenant DataSource associated with the given session.
     * Safe to call when session is null or datasource not present in cache.
     */
    public void closeTenantDataSource(SessionData session) {
        if (session == null) {
            return;
        }

        String resolvedTenantId = TenantContext.buildTenantId(session);

        // Try to obtain and remove the DataSource from the cache
        DataSource ds = cache.getIfPresent(resolvedTenantId);
        if (ds != null) {
            try {
                closeDataSource(ds);
                log.info("Closed tenant datasource: {}", resolvedTenantId);
            } catch (Exception e) {
                log.warn("Error closing datasource for tenant {}", resolvedTenantId, e);
            } finally {
                // remove from cache to avoid returning closed datasource later
                cache.invalidate(resolvedTenantId);
            }
        }
    }

    // =========================
    // REQUIRED METHODS
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
