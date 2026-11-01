package es.ubu.lsi.ubumonitoranalytics.features.system.shutdown.infrastructure.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration for monitoring the process that owns this application. */
@ConfigurationProperties(prefix = "process-monitor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessMonitorProperties {

  private Long parentPid;
}
