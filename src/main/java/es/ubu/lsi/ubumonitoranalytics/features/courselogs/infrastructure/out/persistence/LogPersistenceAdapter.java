package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out.LogPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.importlogs.ProcessLogLine;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import lombok.RequiredArgsConstructor;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Courses.COURSES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Logs.LOGS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsComponents.LOGS_COMPONENTS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsEvents.LOGS_EVENTS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsOrigins.LOGS_ORIGINS;


@Component
@RequiredArgsConstructor
public class LogPersistenceAdapter implements LogPersistencePort {

    private final Jooq jooq;

    @Override
    public boolean existCourse(Integer courseId) {
        DSLContext dsl = jooq.dsl();
        return dsl.fetchExists(
            dsl.selectOne()
                .from(COURSES)
                .where(COURSES.ID.eq(courseId))
        );
    }

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
    public void saveBatch(List<ProcessLogLine> logs) {

        DSLContext dsl = jooq.dsl();

        BatchBindStep batch = dsl.batch(
            dsl.insertInto(LOGS,
                LOGS.TIMESTAMP,
                LOGS.USER_ID,
                LOGS.COURSE_ID,
                LOGS.COMPONENT_ID,
                LOGS.EVENT_ID,
                LOGS.MODULE_ID,
                LOGS.ORIGIN_ID,
                LOGS.IP_ADDRESS
            ).values((LocalDateTime) null, null, null, null, null, null, null, null)
        );
        for (ProcessLogLine l : logs) {
            batch.bind(
                l.getTime(),
                l.getUserId(),
                l.getCourseId(),
                l.getComponentId(),
                l.getEventId(),
                l.getModuleId(),
                l.getOriginId(),
                l.getIpAddress()
            );
        }

        batch.execute();
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

