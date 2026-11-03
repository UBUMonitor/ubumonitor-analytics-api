package es.ubu.lsi.ubumonitoranalytics.features.calendars.infrastructure.out.moodle;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.core.calendar.getcalendarevents.response.GetCalendarEventsResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.calendars.application.port.out.FetchCalendarEventsPort;
import es.ubu.lsi.ubumonitoranalytics.features.calendars.domain.model.CalendarEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MoodleCalendarEventsAdapter implements FetchCalendarEventsPort {

  private final Client client;
  private final MoodleCalendarEventsMapper mapper;

  @Override
  public List<CalendarEvent> fetchCalendarEvents(List<Long> courseIds) {
    GetCalendarEventsResponseApi response =
        client
            .coreCalendar()
            .getCalendarEvents(mapper.toRequest(new CalendarEventsRequestParameters(courseIds)));
    return mapper.toDomain(response.getEvents());
  }
}
