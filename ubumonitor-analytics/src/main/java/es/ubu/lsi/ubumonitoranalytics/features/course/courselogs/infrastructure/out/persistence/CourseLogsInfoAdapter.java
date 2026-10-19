package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Logs.LOGS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsComponents.LOGS_COMPONENTS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsComponentsEvents.LOGS_COMPONENTS_EVENTS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsEvents.LOGS_EVENTS;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.CourseLogsInfoPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.info.CourseComponent;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.info.CourseComponentEvent;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.info.CourseEvent;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.info.CourseLogsInfoResult;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.util.*;
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
    var pairs =
        jooq.dsl()
            .selectDistinct(LOGS.COMPONENT_ID, LOGS.EVENT_ID)
            .from(LOGS)
            .where(LOGS.COURSE_ID.eq(courseId))
            .asTable("pairs");

    var componentId = pairs.field(LOGS.COMPONENT_ID);
    var eventId = pairs.field(LOGS.EVENT_ID);

    var records =
        jooq.dsl()
            .select(
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
                    .eq(componentId)
                    .and(LOGS_COMPONENTS_EVENTS.EVENT_ID.eq(eventId)))
            .join(LOGS_COMPONENTS)
            .on(LOGS_COMPONENTS.ID.eq(LOGS_COMPONENTS_EVENTS.COMPONENT_ID))
            .join(LOGS_EVENTS)
            .on(LOGS_EVENTS.ID.eq(LOGS_COMPONENTS_EVENTS.EVENT_ID))
            .fetch();

    Map<Integer, CourseComponent> componentsById = new LinkedHashMap<>();
    Map<Integer, CourseEvent> eventsById = new LinkedHashMap<>();
    List<CourseComponentEvent> componentEventRows = new ArrayList<>();
    for (Record record : records) {
      CourseComponentEvent pair = toCourseComponentEvent(record);
      componentEventRows.add(pair);
      CourseComponent component = pair.getCourseComponent();
      componentsById.putIfAbsent(component.getId(), component);
      CourseEvent event = new CourseEvent();
      event.setId(record.get(LOGS_EVENTS.ID).intValue());
      event.setCode(record.get(LOGS_EVENTS.CODE));
      event.setName(record.get(LOGS_EVENTS.NAME));
      eventsById.putIfAbsent(event.getId(), event);
    }
    List<CourseEvent> events = new ArrayList<>(eventsById.values());

    CourseLogsInfoResult result = new CourseLogsInfoResult();
    result.setComponents(new ArrayList<>(componentsById.values()));
    result.setEvents(events);
    result.setComponentsEvents(componentEventRows);
    return result;
  }

  private CourseComponentEvent toCourseComponentEvent(Record record) {
    CourseComponentEvent componentEvent = new CourseComponentEvent();
    CourseComponent component = new CourseComponent();
    component.setId(record.get(LOGS_COMPONENTS.ID).intValue());
    component.setCode(record.get(LOGS_COMPONENTS.CODE));
    component.setName(record.get(LOGS_COMPONENTS.NAME));
    componentEvent.setCourseComponent(component);

    CourseEvent event = new CourseEvent();
    event.setId(record.get(LOGS_EVENTS.ID).intValue());
    event.setCode(record.get(LOGS_EVENTS.CODE));
    event.setName(record.get(LOGS_EVENTS.NAME));
    componentEvent.setCourseEvent(event);
    return componentEvent;
  }
}
