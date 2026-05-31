package es.ubu.lsi.ubumonitoranalytics.util;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.DatabaseCreationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.IDN;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class DatabaseUtil {

    public static String toHostString(URI host) {
        if (host == null || host.getHost() == null) {
            return "unknown_host";
        }
        return IDN.toASCII(host.getHost());
    }


    public static String toSafeUserName(String username) {
        if (username == null || username.isBlank()) {
            return "default_user";
        }
        // URLEncoder es la forma más segura de mantener la identidad del usuario
        // sin romper las reglas del sistema de archivos.
        return URLEncoder.encode(username, StandardCharsets.UTF_8);
    }


    public static String buildDbFileName(URI host, String username) {
        return toHostString(host) + "_" + toSafeUserName(username);
    }

    public static String buildJdbcUrl(String jdbcTemplate, String basePath, URI host, String username) {
        String fileName = buildDbFileName(host, username);

        // Usamos Path para normalizar la ruta según el SO (evita problemas de / o \)
        String fullPath = Path.of(basePath)
            .resolve(fileName)
            .toAbsolutePath()
            .toString();

        // Importante: H2 en Windows a veces necesita que las barras invertidas
        // de la ruta sean normales (/) o escapadas en la URL JDBC.
        // toString() de Path suele ser suficiente, pero esto es más robusto:
        String normalizedPath = fullPath.replace("\\", "/");

        return String.format(jdbcTemplate, normalizedPath);
    }


    public static void createAndInitializeDatabase(String jdbcTemplate, String basePath, URI host, String username, String dbPassword) {

        Path dir = Path.of(basePath);

        try {
            Files.createDirectories(dir);

            String jdbcUrl = DatabaseUtil.buildJdbcUrl(
                jdbcTemplate,
                basePath,
                host,
                username
            );

            log.info("ABS PATH DB: {}", dir.toAbsolutePath());
            log.info("FINAL FILE: {}", jdbcUrl);
            log.info("FILE EXISTS: {}", Files.exists(getFile(basePath, host, username)));
            log.info("Ensuring database exists for {} / {}", host, username);

            // Solo “abre” la DB (H2 la crea si no existe)
            try (var _ = DriverManager.getConnection(
                jdbcUrl,
                "sa",
                dbPassword + " " + dbPassword
            )) {
                log.info("Database ready (H2 auto-create)");
            }

        } catch (SQLException e) {
            throw new DatabaseCreationException("Error creating H2 DB", e);
        } catch (Exception e) {
            throw new DatabaseCreationException("Unexpected error", e);
        }
    }

    public static boolean exists(String basePath, URI host, String username) {
        return Files.exists(getFile(basePath, host, username));
    }

    public static Path getFile(String basePath, URI host, String username) {
        String fileName = DatabaseUtil.buildDbFileName(host, username);
        return Path.of(basePath)
            .resolve(fileName + ".mv.db");
    }
}
