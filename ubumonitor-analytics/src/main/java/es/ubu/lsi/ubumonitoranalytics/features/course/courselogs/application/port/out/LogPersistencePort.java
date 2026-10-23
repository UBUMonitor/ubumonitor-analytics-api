package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.LogContext;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.ProcessLogLine;
import java.util.List;

/** Reads and writes course log data in tenant persistence. */
public interface LogPersistencePort {

  /**
   * @param courseId Moodle course identifier
   */
  void createIfNotExists(Long courseId);

  /**
   * @param batch processed log records to persist
   */
  void saveBatch(List<ProcessLogLine> batch);

  LogContext getLogContext(Long courseId);
}
