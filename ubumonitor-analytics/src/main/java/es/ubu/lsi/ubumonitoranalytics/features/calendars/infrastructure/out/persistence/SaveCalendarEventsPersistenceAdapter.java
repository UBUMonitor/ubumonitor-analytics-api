package es.ubu.lsi.ubumonitoranalytics.features.calendars.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.CalendarsEvents.CALENDARS_EVENTS;

import es.ubu.lsi.ubumonitoranalytics.features.calendars.application.port.out.SaveCalendarEventsPort;
import es.ubu.lsi.ubumonitoranalytics.features.calendars.domain.model.CalendarEvent;
import es.ubu.lsi.ubumonitoranalytics.features.calendars.infrastructure.out.persistence.mapper.CalendarEventsPersistenceMapper;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.CalendarsEventsRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.UpdatableRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SaveCalendarEventsPersistenceAdapter implements SaveCalendarEventsPort {

  private final Jooq jooq;
  private final CalendarEventsPersistenceMapper mapper;

  @Override
  @Transactional
  public void saveCalendarEvents(List<Long> courseIds, List<CalendarEvent> events) {
    var delete = jooq.dsl().deleteFrom(CALENDARS_EVENTS);
    if (courseIds == null || courseIds.isEmpty()) {
      delete.execute();
    } else {
      delete
          .where(
              CALENDARS_EVENTS.COURSE_ID.in(courseIds).or(CALENDARS_EVENTS.EVENT_TYPE.eq("site")))
          .execute();
    }

    Long now = Instant.now().getEpochSecond();
    List<UpdatableRecord<?>> records = new ArrayList<>();
    for (CalendarEvent event : events) {
      CalendarsEventsRecord record = jooq.dsl().newRecord(CALENDARS_EVENTS);
      mapper.toRecord(event, record);
      record.setCreatedAt(now);
      record.setUpdatedAt(now);
      records.add(record);
    }
    if (!records.isEmpty()) {
      jooq.dsl().batchMerge(records).execute();
    }
  }
}
