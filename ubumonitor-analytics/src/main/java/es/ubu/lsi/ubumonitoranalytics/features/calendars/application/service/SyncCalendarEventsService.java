package es.ubu.lsi.ubumonitoranalytics.features.calendars.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.calendars.application.port.in.SyncCalendarEventsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.calendars.application.port.out.FetchCalendarEventsPort;
import es.ubu.lsi.ubumonitoranalytics.features.calendars.application.port.out.SaveCalendarEventsPort;
import es.ubu.lsi.ubumonitoranalytics.features.calendars.domain.model.CalendarEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncCalendarEventsService implements SyncCalendarEventsUseCase {

  private final FetchCalendarEventsPort fetchCalendarEventsPort;
  private final SaveCalendarEventsPort saveCalendarEventsPort;

  @Override
  public List<CalendarEvent> syncCalendarEvents(List<Long> courseIds) {
    List<CalendarEvent> events = fetchCalendarEventsPort.fetchCalendarEvents(courseIds);
    saveCalendarEventsPort.saveCalendarEvents(courseIds, events);
    return events;
  }
}
