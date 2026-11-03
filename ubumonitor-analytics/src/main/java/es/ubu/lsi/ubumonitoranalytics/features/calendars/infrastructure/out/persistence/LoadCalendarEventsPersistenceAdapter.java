package es.ubu.lsi.ubumonitoranalytics.features.calendars.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.CalendarsEvents.CALENDARS_EVENTS;

import es.ubu.lsi.ubumonitoranalytics.features.calendars.application.port.out.LoadCalendarEventsPort;
import es.ubu.lsi.ubumonitoranalytics.features.calendars.domain.model.CalendarEvent;
import es.ubu.lsi.ubumonitoranalytics.features.calendars.infrastructure.out.persistence.mapper.CalendarEventsPersistenceMapper;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoadCalendarEventsPersistenceAdapter implements LoadCalendarEventsPort {

  private final Jooq jooq;
  private final CalendarEventsPersistenceMapper mapper;

  @Override
  public List<CalendarEvent> loadCalendarEvents(List<Long> courseIds) {
    var query = jooq.dsl().selectFrom(CALENDARS_EVENTS);
    if (courseIds != null && !courseIds.isEmpty()) {
      query.where(
          CALENDARS_EVENTS
              .COURSE_ID
              .in(courseIds)
              .or(CALENDARS_EVENTS.COURSE_ID.eq(1L).and(CALENDARS_EVENTS.EVENT_TYPE.eq("site"))));
    }
    return query.orderBy(CALENDARS_EVENTS.TIME_START, CALENDARS_EVENTS.ID).fetch(mapper::toDomain);
  }
}
