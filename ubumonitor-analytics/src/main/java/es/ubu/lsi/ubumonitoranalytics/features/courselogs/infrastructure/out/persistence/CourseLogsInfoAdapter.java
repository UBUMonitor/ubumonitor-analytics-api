package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Logs.LOGS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsComponents.LOGS_COMPONENTS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.LogsEvents.LOGS_EVENTS;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out.CourseLogsInfoPort;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info.CourseComponent;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info.CourseComponentEvent;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info.CourseEvent;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info.CourseLogsInfoResult;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.jooq.Record;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseLogsInfoAdapter implements CourseLogsInfoPort {

  private final Jooq jooq;

  @Override
  public CourseLogsInfoResult getCourseLogsInfo(Integer courseId) {
    var pairs =
        jooq.dsl()
            .selectDistinct(LOGS.COMPONENT_ID, LOGS.EVENT_ID)
            .from(LOGS)
            .where(LOGS.COURSE_ID.eq(courseId))
            .asTable("pairs");

    var componentId = pairs.field(LOGS.COMPONENT_ID);
    var eventId = pairs.field(LOGS.EVENT_ID);

    List<CourseComponentEvent> componentEventRows =
        jooq.dsl()
            .select(LOGS_COMPONENTS.ID, LOGS_COMPONENTS.NAME, LOGS_EVENTS.ID, LOGS_EVENTS.NAME)
            .from(pairs)
            .join(LOGS_COMPONENTS)
            .on(LOGS_COMPONENTS.ID.eq(componentId))
            .join(LOGS_EVENTS)
            .on(LOGS_EVENTS.ID.eq(eventId))
            .orderBy(LOGS_COMPONENTS.NAME.asc(), LOGS_EVENTS.NAME.asc())
            .fetch(this::toCourseComponentEvent);

    Map<Integer, CourseComponent> componentsById = new LinkedHashMap<>();
    Map<Integer, CourseEvent> eventsById = new HashMap<>();
    for (CourseComponentEvent pair : componentEventRows) {
      CourseComponent component = pair.getCourseComponent();
      CourseEvent event = pair.getCourseEvent();
      componentsById.putIfAbsent(component.getId(), component);
      eventsById.putIfAbsent(event.getId(), event);
    }
    List<CourseEvent> events = new ArrayList<>(eventsById.values());
    events.sort(Comparator.comparing(CourseEvent::getName));

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
    component.setName(record.get(LOGS_COMPONENTS.NAME));
    componentEvent.setCourseComponent(component);

    CourseEvent event = new CourseEvent();
    event.setId(record.get(LOGS_EVENTS.ID).intValue());
    event.setName(record.get(LOGS_EVENTS.NAME));
    componentEvent.setCourseEvent(event);
    return componentEvent;
  }
}
