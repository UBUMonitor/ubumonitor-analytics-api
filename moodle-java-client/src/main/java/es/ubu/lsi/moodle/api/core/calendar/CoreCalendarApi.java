package es.ubu.lsi.moodle.api.core.calendar;

import es.ubu.lsi.moodle.model.core.calendar.getcalendarevents.request.GetCalendarEventsRequestApi;
import es.ubu.lsi.moodle.model.core.calendar.getcalendarevents.response.GetCalendarEventsResponseApi;

public interface CoreCalendarApi {
  GetCalendarEventsResponseApi getCalendarEvents(GetCalendarEventsRequestApi request);
}
