package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.ProcessLogLine;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface LogPersistencePort {

    Map<String, Byte> getLogComponents();

    Map<String, Short> getLogEvents();

    Map<String, Byte> getLogOrigins();

    void saveBatch(List<ProcessLogLine> batch, SessionData sessionData);

    LocalDateTime getLastDateTime(Integer courseId);
}
