package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out.LogPersistencePort;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.LogLine;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.LogComponentEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.LogEventEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.JdbcTemplateFactory;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.LogComponentRepository;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.LogEventRepository;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class LogPersistenceAdapter implements LogPersistencePort {

    private final LogComponentRepository logComponentRepository;
    private final LogEventRepository logEventRepository;
    private final JdbcTemplateFactory jdbcTemplateFactory;
    private final LogRepository logRepository;

    @Override
    public Map<String, Byte> getLogComponents() {
        return logComponentRepository.findAll().stream()
            .collect(Collectors.toMap(
                LogComponentEntity::getName,
                LogComponentEntity::getId
            ));
    }

    @Override
    public Map<String, Short> getLogEvents() {
        return logEventRepository.findAll().stream()
            .collect(Collectors.toMap(
                LogEventEntity::getName,
                LogEventEntity::getId
            ));
    }

    @Override
    public void saveBatch(List<LogLine> logs) {
        JdbcTemplate jdbcTemplate = jdbcTemplateFactory.getJdbcTemplate();
        jdbcTemplate.batchUpdate("""
            INSERT INTO logs (
                timestamp,
                user_id,
                course_id,
                component_id,
                event_id,
                module_id
            )
            VALUES (?, ?, ?, ?, ?, ?)
        """,
            logs,
            logs.size(),
            (ps, log) -> {

                ps.setObject(1, log.getTime());
                ps.setObject(2, log.getUserId());     // puede ser null, por eso se usa setObject
                ps.setInt(3, log.getCourseId());
                ps.setByte(4, log.getComponentId());
                ps.setShort(5, log.getEventId());
                ps.setObject(6, log.getModuleId());   // puede ser null, por eso se usa setObject
            });
    }

    @Override
    public LocalDateTime getLastDateTime(Integer courseId) {
        return logRepository.findLastTimestampByCourseId(courseId);
    }


}

