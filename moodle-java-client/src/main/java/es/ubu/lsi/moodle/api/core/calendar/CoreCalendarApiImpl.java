package es.ubu.lsi.moodle.api.core.calendar;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.core.calendar.getcalendarevents.request.GetCalendarEventsRequestApi;
import es.ubu.lsi.moodle.model.core.calendar.getcalendarevents.response.GetCalendarEventsResponseApi;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CoreCalendarApiImpl implements CoreCalendarApi {

  private final Client client;

  @Override
  public GetCalendarEventsResponseApi getCalendarEvents(GetCalendarEventsRequestApi request) {
    return client.execute(request, GetCalendarEventsResponseApi.class);
  }
}
