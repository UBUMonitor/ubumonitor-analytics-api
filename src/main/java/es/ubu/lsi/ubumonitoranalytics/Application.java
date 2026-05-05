package es.ubu.lsi.ubumonitoranalytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;


@SpringBootApplication
@ConfigurationPropertiesScan
@EnableWebSecurity
@EnableJpaAuditing
@EnableJpaRepositories
public class Application {

    /**
     * Application entry point.
     *
     * This method is automatically called by the JVM when the application starts.
     * It initializes the Spring Boot application context and starts the embedded Tomcat server.
     *
     * Usage example:
     * {@code
     * java -jar target/moodle-openapi-adapter-0.0.1-SNAPSHOT.jar
     * }
     *
     * @param args Command-line arguments that can override configuration properties.
     *             Examples:
     *             - {@code --spring.profiles.active=dev} to activate development profile
     *             - {@code --server.port=9090} to change server port
     *             - {@code --logging.level.root=DEBUG} to enable debug logging
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

