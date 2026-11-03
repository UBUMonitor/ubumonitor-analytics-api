package es.ubu.lsi.ubumonitoranalytics.features.calendars.infrastructure.out.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import es.ubu.lsi.ubumonitoranalytics.features.calendars.domain.model.CalendarEvent;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.CalendarsEventsRecord;
import org.junit.jupiter.api.Test;

class CalendarEventsPersistenceMapperTest {

  private final CalendarEventsPersistenceMapper mapper = new CalendarEventsPersistenceMapperImpl();

  @Test
  void shouldStoreNullForNonPositiveGroupId() {
    CalendarsEventsRecord record = new CalendarsEventsRecord();

    mapper.toRecord(calendarEvent(0L), record);

    assertNull(record.getGroupId());
  }

  @Test
  void shouldStorePositiveGroupId() {
    CalendarsEventsRecord record = new CalendarsEventsRecord();

    mapper.toRecord(calendarEvent(7L), record);

    assertEquals(7L, record.getGroupId());
  }

  private static CalendarEvent calendarEvent(Long groupId) {
    return new CalendarEvent(
        1L, "event", null, 2L, null, groupId, 3L, null, null, null, "course", 4L, 0L, 1, 1, 5L,
        null, null);
  }
}
