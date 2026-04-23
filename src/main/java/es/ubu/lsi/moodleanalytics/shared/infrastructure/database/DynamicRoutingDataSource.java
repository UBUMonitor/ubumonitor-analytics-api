    package es.ubu.lsi.moodleanalytics.shared.infrastructure.database;

    import com.zaxxer.hikari.HikariDataSource;
    import es.ubu.lsi.moodleanalytics.shared.domain.model.SessionData;
    import es.ubu.lsi.moodleanalytics.shared.infrastructure.exception.DatabaseConnectionException;
    import es.ubu.lsi.moodleanalytics.shared.infrastructure.moodle.config.MoodleConfig;
    import es.ubu.lsi.moodleanalytics.shared.infrastructure.session.CurrentSessionContext;
    import es.ubu.lsi.moodleanalytics.util.DatabaseUtil;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.jspecify.annotations.NonNull;
    import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
    import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
    import java.util.Properties;
    import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

    import javax.sql.DataSource;
    import java.net.URI;
    import java.util.Map;
    import java.util.concurrent.ConcurrentHashMap;

    @RequiredArgsConstructor
    @Slf4j
    public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

        private final CurrentSessionContext currentSessionContext;
        private final MoodleConfig moodleConfig;

        private final Map<Object, DataSource> activeDataSources = new ConcurrentHashMap<>();

        @Override
        protected Object determineCurrentLookupKey() {

            SessionData session = currentSessionContext.getSessionData();

            if (session == null) {
                return "bootstrap";
            }

            return session.getHost() + "_" + session.getUserId();
        }

        @Override
        protected DataSource determineTargetDataSource() {

            Object key = determineCurrentLookupKey();

            DataSource ds = activeDataSources.get(key);

            if (ds != null) {
                return ds;
            }

            if ("bootstrap".equals(key)) {
                return super.getResolvedDefaultDataSource();
            }

            SessionData session = currentSessionContext.getSessionData();

            if (session == null) {
                return super.getResolvedDefaultDataSource();
            }

            return activeDataSources.computeIfAbsent(key, k -> createH2(session));
        }

        private DataSource createH2(SessionData session) {

            HikariDataSource ds = new HikariDataSource();

            String jdbcUrl = String.format(
                "jdbc:h2:file:%s/%s_%s;CIPHER=AES",
                moodleConfig.getDb().getBasePath(),
                DatabaseUtil.toHostString(session.getHost()),
                session.getUserId()
            );

            ds.setJdbcUrl(jdbcUrl);
            ds.setUsername("sa");
            ds.setPassword(session.getDbPassword() + " " + session.getDbPassword());
            ds.setDriverClassName("org.h2.Driver");

            ds.setMaximumPoolSize(5);
            ds.setConnectionTimeout(3000);


            try (var conn = ds.getConnection()) {
                // usar la conexión para forzar la creación del fichero/BD
                conn.getMetaData();
                log.info("DB creada correctamente: {}", lookupKey(session));
            } catch (Exception e) {
                throw new DatabaseConnectionException("Error conectando BD", e);
            }

            // Usar Hibernate para crear/actualizar el esquema (hbm2ddl.auto=update)
            // Creamos un EntityManagerFactory temporal apuntando a esta DataSource
            LocalContainerEntityManagerFactoryBean tempEmf = getLocalContainerEntityManagerFactoryBean(ds);

            try {
                tempEmf.afterPropertiesSet();
                log.info("Schema creado/actualizado por Hibernate para: {}", lookupKey(session));
            } catch (Exception e) {
                log.warn("No se pudo crear el esquema con Hibernate para {}: {}", lookupKey(session), e.getMessage());
            } finally {
                // Cerrar/limpiar el EMF temporal para no dejar recursos abiertos
                try {
                    tempEmf.destroy();
                } catch (Exception e) {
                    // ignoramos errores de limpieza
                    log.debug("Error cerrando EMF temporal: {}", e.getMessage());
                }
            }

            return ds;
        }

        private static @NonNull LocalContainerEntityManagerFactoryBean getLocalContainerEntityManagerFactoryBean(HikariDataSource ds) {
            LocalContainerEntityManagerFactoryBean tempEmf = new LocalContainerEntityManagerFactoryBean();
            tempEmf.setDataSource(ds);
            tempEmf.setPackagesToScan("es.ubu.lsi.moodleanalytics");
            tempEmf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

            Properties jpaProps = new Properties();
            jpaProps.put("hibernate.hbm2ddl.auto", "update");
            jpaProps.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            tempEmf.setJpaProperties(jpaProps);
            return tempEmf;
        }

        private String lookupKey(SessionData session) {
            return session.getHost() + "_" + session.getUserId();
        }

        public void closeTenant(URI host, String userId) {

            String key = DatabaseUtil.toHostString(host) + "_" + userId;

            DataSource ds = activeDataSources.remove(key);

            if (ds instanceof HikariDataSource hikari) {
                hikari.close();
                log.info("Pool cerrado: {}", key);
            }
        }
    }
