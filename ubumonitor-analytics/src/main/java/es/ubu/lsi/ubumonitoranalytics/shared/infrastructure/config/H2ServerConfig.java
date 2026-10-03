package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.config;

import java.sql.SQLException;
import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Configures the optional H2 database server used by the application. */
@Configuration
@Profile("dev")
public class H2ServerConfig {

  @Bean(initMethod = "start", destroyMethod = "stop")
  public Server h2TcpServer() throws SQLException {
    return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092");
  }
}
