package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.importlogs.ProcessLogLine;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Reads and writes course log data in tenant persistence. */
public interface LogPersistencePort {

  /**
   * @param courseId Moodle course identifier
   */
  void createIfNotExists(Integer courseId);

  /**
   * @return log component names mapped to local identifiers
   */
  Map<String, Byte> getLogComponents();

  /**
   * @return event names mapped to local identifiers
   */
  Map<String, Short> getLogEvents();

  /**
   * @return log origin names mapped to local identifiers
   */
  Map<String, Byte> getLogOrigins();

  /**
   * @param batch processed log records to persist
   */
  void saveBatch(List<ProcessLogLine> batch);

  /**
   * @param courseId Moodle course identifier
   * @return timestamp of the latest stored log, or {@code null} when none exists
   */
  LocalDateTime getLastDateTime(Integer courseId);
}
