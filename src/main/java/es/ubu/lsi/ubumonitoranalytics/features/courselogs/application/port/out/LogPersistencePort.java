package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.LogLine;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface LogPersistencePort {

    Map<String, Byte> getLogComponents();

    Map<String, Short> getLogEvents();

    void saveBatch(List<LogLine> batch);

    LocalDateTime getLastDateTime(Integer courseId);
}
