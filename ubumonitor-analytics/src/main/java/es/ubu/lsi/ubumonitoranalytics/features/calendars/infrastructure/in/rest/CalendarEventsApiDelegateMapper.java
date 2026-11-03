package es.ubu.lsi.ubumonitoranalytics.features.calendars.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CalendarEventDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CalendarEventsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.calendars.domain.model.CalendarEvent;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface CalendarEventsApiDelegateMapper {

  default CalendarEventsResponseDto toDto(List<CalendarEvent> events) {
    CalendarEventsResponseDto response = new CalendarEventsResponseDto();
    response.setEvents(toEventDtos(events));
    return response;
  }

  List<CalendarEventDto> toEventDtos(List<CalendarEvent> events);

  CalendarEventDto toDto(CalendarEvent event);
}
