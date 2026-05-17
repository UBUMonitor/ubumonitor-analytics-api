package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out.LogPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.LogLine;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.LogsRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import lombok.RequiredArgsConstructor;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Logs.LOGS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsComponents.LOGS_COMPONENTS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsEvents.LOGS_EVENTS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsOrigins.LOGS_ORIGINS;


@Component
@RequiredArgsConstructor
public class LogPersistenceAdapter implements LogPersistencePort {

    private final Jooq jooq;

    @Override
    public Map<String, Byte> getLogComponents() {
        return jooq.dsl()
            .select(LOGS_COMPONENTS.NAME, LOGS_COMPONENTS.ID)
            .from(LOGS_COMPONENTS)
            .fetchMap(
                LOGS_COMPONENTS.NAME,
                LOGS_COMPONENTS.ID
            );
    }

    @Override
    public Map<String, Short> getLogEvents() {
        return jooq.dsl()
            .select(LOGS_EVENTS.NAME, LOGS_EVENTS.ID)
            .from(LOGS_EVENTS)
            .fetchMap(
                LOGS_EVENTS.NAME,
                LOGS_EVENTS.ID
            );
    }

    @Override
    public Map<String, Byte> getLogOrigins() {
        return jooq.dsl()
            .select(LOGS_ORIGINS.NAME, LOGS_ORIGINS.ID)
            .from(LOGS_ORIGINS)
            .fetchMap(
                LOGS_ORIGINS.NAME,
                LOGS_ORIGINS.ID
            );
    }

    @Override
    @Transactional
    public void saveBatch(List<LogLine> logs) {
        List<LogsRecord> records = logs.stream()
            .map(this::toRecord)
            .toList();

        jooq.dsl().batchInsert(records).execute();
    }

    private LogsRecord toRecord(LogLine log) {
        LogsRecord r = jooq.dsl().newRecord(LOGS);

        r.setTimestamp(log.getTime());
        r.setUserId(log.getUserId());
        r.setCourseId(log.getCourseId());
        r.setComponentId(log.getComponentId());
        r.setEventId(log.getEventId());
        r.setModuleId(log.getModuleId());
        r.setOriginId(log.getOriginId());
        r.setIpAddress(log.getIpAddress());

        return r;
    }

    @Override
    public LocalDateTime getLastDateTime(Integer courseId) {
        return jooq.dsl()
            .select(DSL.max(LOGS.TIMESTAMP))
            .from(LOGS)
            .where(LOGS.COURSE_ID.eq(courseId))
            .fetchOneInto(LocalDateTime.class);
    }
}

