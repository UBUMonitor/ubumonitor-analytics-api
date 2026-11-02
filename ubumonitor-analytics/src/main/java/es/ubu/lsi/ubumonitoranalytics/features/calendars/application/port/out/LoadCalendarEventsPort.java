package es.ubu.lsi.ubumonitoranalytics.features.calendars.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.calendars.domain.model.CalendarEvent;
import java.util.List;

public interface LoadCalendarEventsPort {
  List<CalendarEvent> loadCalendarEvents(List<Long> courseIds);
}
