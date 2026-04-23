package es.ubu.lsi.moodleanalytics.features.auth.infrastructure.out.database;

import com.zaxxer.hikari.HikariDataSource;
import es.ubu.lsi.moodleanalytics.features.auth.application.port.out.DatabaseManagementPort;
import es.ubu.lsi.moodleanalytics.shared.domain.exception.DatabaseAlreadyExistsException;
import es.ubu.lsi.moodleanalytics.shared.infrastructure.exception.DatabaseCreationException;
import es.ubu.lsi.moodleanalytics.shared.infrastructure.moodle.config.MoodleConfig;
import es.ubu.lsi.moodleanalytics.util.DatabaseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;


import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;

@Component
@RequiredArgsConstructor
@Slf4j
public class H2ManagementAdapter implements DatabaseManagementPort {
    private final MoodleConfig moodleConfig;

    @Override
    public void createAndInitializeDatabase(URI host, String userName, String dbPassword) {
        try {

            // Crear carpeta si no existe
            Path dir = Path.of(moodleConfig.getDb().getBasePath());
            Files.createDirectories(dir);

            Path dbPath = getFile(host, userName);
            if  (Files.exists(dbPath)) {
                throw new DatabaseAlreadyExistsException(dbPath.toString());
            }

            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl("jdbc:h2:file:" + dbPath + ";CIPHER=AES;DB_CLOSE_ON_EXIT=FALSE");
            ds.setUsername("sa");
            ds.setPassword(dbPassword + " " + dbPassword);
            ds.setDriverClassName("org.h2.Driver");

            // Solo abrir conexión -> JPA creará el schema automáticamente
            try (Connection connection = ds.getConnection()) {
                log.info("Creating H2 database {}...", connection.getClientInfo());
            }

            ds.close();

        } catch (Exception e) {
            throw new DatabaseCreationException("Error al crear BD H2 encriptada", e);
        }
    }

    @Override
    public boolean exists(URI host, String userName) {
        return Files.exists(getFile(host, userName));
    }

    private Path getFile(URI host, String userName) {
        String hostName = DatabaseUtil.toHostString(host);
        Path dir = Path.of(moodleConfig.getDb().getBasePath());
        return dir.resolve(hostName + "_" + userName);
    }
}
