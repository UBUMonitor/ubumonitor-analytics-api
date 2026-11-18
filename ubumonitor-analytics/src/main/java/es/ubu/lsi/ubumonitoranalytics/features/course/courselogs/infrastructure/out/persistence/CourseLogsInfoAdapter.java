package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Logs.LOGS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsComponents.LOGS_COMPONENTS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsComponentsEvents.LOGS_COMPONENTS_EVENTS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsEvents.LOGS_EVENTS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsOrigins.LOGS_ORIGINS;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.CourseLogsInfoPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.info.*;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jooq.Record;
import org.springframework.stereotype.Component;

/** Reads available course log fields and filter values from persistence. */
@Component
@RequiredArgsConstructor
public class CourseLogsInfoAdapter implements CourseLogsInfoPort {

  private final Jooq jooq;

  @Override
  public CourseLogsInfoResult getCourseLogsInfo(Long courseId) {
    List<CourseComponentEvent> componentsEvents = fetchComponentsEvents(courseId);

    CourseLogsInfoResult result = new CourseLogsInfoResult();
    result.setComponents(extractDistinctComponents(componentsEvents));
    result.setEvents(extractDistinctEvents(componentsEvents));
    result.setComponentsEvents(componentsEvents);
    result.setOrigins(fetchOrigins(courseId));
    return result;
  }

  /** Component/event pairs that actually appear in the logs of the course. */
  private List<CourseComponentEvent> fetchComponentsEvents(Long courseId) {
    var pairs =
        jooq.dsl()
            .selectDistinct(LOGS.COMPONENT_ID, LOGS.EVENT_ID)
            .from(LOGS)
            .where(LOGS.COURSE_ID.eq(courseId))
            .asTable("pairs");

    var pairComponentId = pairs.field(LOGS.COMPONENT_ID);
    var pairEventId = pairs.field(LOGS.EVENT_ID);

    var records =
        jooq.dsl()
            .selectDistinct(
                LOGS_COMPONENTS.ID,
                LOGS_COMPONENTS.CODE,
                LOGS_COMPONENTS.NAME,
                LOGS_EVENTS.ID,
                LOGS_EVENTS.CODE,
                LOGS_EVENTS.NAME)
            .from(pairs)
            .join(LOGS_COMPONENTS_EVENTS)
            .on(
                LOGS_COMPONENTS_EVENTS
                    .COMPONENT_ID
                    .eq(pairComponentId)
                    .and(LOGS_COMPONENTS_EVENTS.EVENT_ID.eq(pairEventId)))
            .join(LOGS_COMPONENTS)
            .on(LOGS_COMPONENTS.ID.eq(LOGS_COMPONENTS_EVENTS.COMPONENT_ID))
            .join(LOGS_EVENTS)
            .on(LOGS_EVENTS.ID.eq(LOGS_COMPONENTS_EVENTS.EVENT_ID))
            .fetch();

    List<CourseComponentEvent> componentsEvents = new ArrayList<>();
    for (Record record : records) {
      componentsEvents.add(toCourseComponentEvent(record));
    }
    return componentsEvents;
  }

  private List<CourseOrigin> fetchOrigins(Long courseId) {
    var courseOriginIds =
        jooq.dsl().selectDistinct(LOGS.ORIGIN_ID).from(LOGS).where(LOGS.COURSE_ID.eq(courseId));

    var records =
        jooq.dsl()
            .select(LOGS_ORIGINS.ID, LOGS_ORIGINS.CODE, LOGS_ORIGINS.NAME)
            .from(LOGS_ORIGINS)
            .where(LOGS_ORIGINS.ID.in(courseOriginIds))
            .orderBy(LOGS_ORIGINS.ID.asc())
            .fetch();

    List<CourseOrigin> origins = new ArrayList<>();
    for (Record record : records) {
      origins.add(toCourseOrigin(record));
    }
    return origins;
  }

  /** One component per id, keeping the first occurrence and the original order. */
  private List<CourseComponent> extractDistinctComponents(
      List<CourseComponentEvent> componentsEvents) {
    Map<Integer, CourseComponent> componentsById = new LinkedHashMap<>();
    for (CourseComponentEvent componentEvent : componentsEvents) {
      CourseComponent component = componentEvent.getCourseComponent();
      componentsById.putIfAbsent(component.getId(), component);
    }
    return new ArrayList<>(componentsById.values());
  }

  /** One event per id, keeping the first occurrence and the original order. */
  private List<CourseEvent> extractDistinctEvents(List<CourseComponentEvent> componentsEvents) {
    Map<Integer, CourseEvent> eventsById = new LinkedHashMap<>();
    for (CourseComponentEvent componentEvent : componentsEvents) {
      CourseEvent event = componentEvent.getCourseEvent();
      eventsById.putIfAbsent(event.getId(), event);
    }
    return new ArrayList<>(eventsById.values());
  }

  private CourseComponentEvent toCourseComponentEvent(Record record) {
    CourseComponentEvent componentEvent = new CourseComponentEvent();
    componentEvent.setCourseComponent(toCourseComponent(record));
    componentEvent.setCourseEvent(toCourseEvent(record));
    return componentEvent;
  }

  private CourseComponent toCourseComponent(Record record) {
    CourseComponent component = new CourseComponent();
    component.setId(record.get(LOGS_COMPONENTS.ID).intValue());
    component.setCode(record.get(LOGS_COMPONENTS.CODE));
    component.setName(record.get(LOGS_COMPONENTS.NAME));
    return component;
  }

  private CourseEvent toCourseEvent(Record record) {
    CourseEvent event = new CourseEvent();
    event.setId(record.get(LOGS_EVENTS.ID).intValue());
    event.setCode(record.get(LOGS_EVENTS.CODE));
    event.setName(record.get(LOGS_EVENTS.NAME));
    return event;
  }

  private CourseOrigin toCourseOrigin(Record record) {
    CourseOrigin origin = new CourseOrigin();
    origin.setId(record.get(LOGS_ORIGINS.ID).intValue());
    origin.setCode(record.get(LOGS_ORIGINS.CODE));
    origin.setName(record.get(LOGS_ORIGINS.NAME));
    return origin;
  }
}
