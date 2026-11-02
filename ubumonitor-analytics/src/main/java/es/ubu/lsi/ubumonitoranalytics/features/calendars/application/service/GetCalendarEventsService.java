package es.ubu.lsi.ubumonitoranalytics.features.calendars.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.calendars.application.port.in.GetCalendarEventsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.calendars.application.port.out.LoadCalendarEventsPort;
import es.ubu.lsi.ubumonitoranalytics.features.calendars.domain.model.CalendarEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCalendarEventsService implements GetCalendarEventsUseCase {

  private final LoadCalendarEventsPort loadCalendarEventsPort;

  @Override
  @Transactional(readOnly = true)
  public List<CalendarEvent> getCalendarEvents(List<Long> courseIds) {
    return loadCalendarEventsPort.loadCalendarEvents(courseIds);
  }
}
