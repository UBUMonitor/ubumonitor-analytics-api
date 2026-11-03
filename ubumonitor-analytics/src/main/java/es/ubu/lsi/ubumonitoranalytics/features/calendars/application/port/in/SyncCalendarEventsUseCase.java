package es.ubu.lsi.ubumonitoranalytics.features.calendars.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.calendars.domain.model.CalendarEvent;
import java.util.List;

public interface SyncCalendarEventsUseCase {
  List<CalendarEvent> syncCalendarEvents(List<Long> courseIds);
}
