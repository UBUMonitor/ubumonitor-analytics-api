package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.importlogs.ProcessLogLine;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface LogPersistencePort {

    boolean existCourse(Integer courseId);

    Map<String, Byte> getLogComponents();

    Map<String, Short> getLogEvents();

    Map<String, Byte> getLogOrigins();

    void saveBatch(List<ProcessLogLine> batch);

    LocalDateTime getLastDateTime(Integer courseId);
}
