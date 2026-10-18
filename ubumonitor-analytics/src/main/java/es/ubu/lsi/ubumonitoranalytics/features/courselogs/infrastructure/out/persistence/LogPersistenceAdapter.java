package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out.LogPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out.MoodleLogCatalog;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.importlogs.ProcessLogLine;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.rules.MoodleLogRule;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.config.MoodleRulesConfig;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import lombok.RequiredArgsConstructor;
import org.jooq.BatchBindStep;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Courses.COURSES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Logs.LOGS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsComponents.LOGS_COMPONENTS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsComponentsEvents.LOGS_COMPONENTS_EVENTS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsEvents.LOGS_EVENTS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsOrigins.LOGS_ORIGINS;

/** Stores course log records and provides their lookup metadata. */
@Component
@RequiredArgsConstructor
public class LogPersistenceAdapter implements LogPersistencePort {

  private final Jooq jooq;
  private final MoodleRulesConfig moodleRulesConfig;

  @Override
  public void createIfNotExists(Integer courseId) {
    DSLContext dsl = jooq.dsl();

    boolean exists = dsl.fetchExists(dsl.selectOne().from(COURSES).where(COURSES.ID.eq(courseId)));

    if (!exists) {
      dsl.insertInto(COURSES).set(COURSES.ID, courseId).execute();
    }
  }

  @Override
  public MoodleLogCatalog getMoodleLogCatalog() {
    var records =
        jooq.dsl()
            .select(
                LOGS_COMPONENTS.ID,
                LOGS_COMPONENTS.CODE,
                LOGS_COMPONENTS.NAME,
                LOGS_EVENTS.ID,
                LOGS_EVENTS.CODE,
                LOGS_EVENTS.NAME,
                LOGS_COMPONENTS_EVENTS.GROK_PATTERN_EN)
            .from(LOGS_COMPONENTS)
            .leftJoin(LOGS_COMPONENTS_EVENTS)
            .on(LOGS_COMPONENTS.ID.eq(LOGS_COMPONENTS_EVENTS.COMPONENT_ID))
            .leftJoin(LOGS_EVENTS)
            .on(LOGS_EVENTS.ID.eq(LOGS_COMPONENTS_EVENTS.EVENT_ID))
            .fetch();

    List<MoodleLogRule> rules = new ArrayList<>(records.size());
    for (var record : records) {
      Short eventId = record.get(LOGS_EVENTS.ID);
      if (eventId == null) {
        continue;
      }

      rules.add(
          new MoodleLogRule(
              record.get(LOGS_COMPONENTS.ID),
              record.get(LOGS_COMPONENTS.CODE),
              record.get(LOGS_COMPONENTS.NAME),
              eventId,
              record.get(LOGS_EVENTS.CODE),
              record.get(LOGS_EVENTS.NAME),
              record.get(LOGS_COMPONENTS_EVENTS.GROK_PATTERN_EN)));
    }

    return moodleRulesConfig.compileRules(rules);
  }

  @Override
  public Map<String, Byte> getLogOrigins() {
    return jooq.dsl()
        .select(LOGS_ORIGINS.NAME, LOGS_ORIGINS.ID)
        .from(LOGS_ORIGINS)
        .fetchMap(LOGS_ORIGINS.NAME, LOGS_ORIGINS.ID);
  }

  @Override
  public void saveBatch(List<ProcessLogLine> logs) {

    DSLContext dsl = jooq.dsl();

    BatchBindStep batch =
        dsl.batch(
            dsl.insertInto(
                    LOGS,
                    LOGS.TIMESTAMP,
                    LOGS.USER_ID,
                    LOGS.COURSE_ID,
                    LOGS.COMPONENT_ID,
                    LOGS.EVENT_ID,
                    LOGS.MODULE_ID,
                    LOGS.ORIGIN_ID,
                    LOGS.IP_ADDRESS)
                .values((LocalDateTime) null, null, null, null, null, null, null, null));
    for (ProcessLogLine l : logs) {
      batch.bind(
          l.getTime(),
          l.getUserId(),
          l.getCourseId(),
          l.getComponentId(),
          l.getEventId(),
          l.getModuleId(),
          l.getOriginId(),
          l.getIpAddress());
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
