package es.ubu.lsi.ubumonitoranalytics.features.calendars.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.CalendarsApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CalendarEventsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.calendars.application.port.in.GetCalendarEventsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.calendars.application.port.in.SyncCalendarEventsUseCase;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CalendarEventsApiDelegateImpl implements CalendarsApiDelegate {

  private final SyncCalendarEventsUseCase syncCalendarEventsUseCase;
  private final GetCalendarEventsUseCase getCalendarEventsUseCase;
  private final CalendarEventsApiDelegateMapper calendarEventsApiDelegateMapper;

  @Override
  public ResponseEntity<CalendarEventsResponseDto> syncCalendarEvents(List<Long> courseIds) {
    return ResponseEntity.ok(
        calendarEventsApiDelegateMapper.toDto(
            syncCalendarEventsUseCase.syncCalendarEvents(courseIds)));
  }

  @Override
  public ResponseEntity<CalendarEventsResponseDto> getCalendarEventsInfo(List<Long> courseIds) {
    return ResponseEntity.ok(
        calendarEventsApiDelegateMapper.toDto(
            getCalendarEventsUseCase.getCalendarEvents(courseIds)));
  }
}
