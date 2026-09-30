package es.ubu.lsi.moodle.api.core.calendar;

import es.ubu.lsi.moodle.model.core.calendar.getcalendarevents.request.GetCalendarEventsRequestApi;
import es.ubu.lsi.moodle.model.core.calendar.getcalendarevents.response.GetCalendarEventsResponseApi;

/** Exposes Moodle calendar operations. */
public interface CoreCalendarApi {
  /**
   * @param request calendar event query
   * @return matching calendar events
   */
  GetCalendarEventsResponseApi getCalendarEvents(GetCalendarEventsRequestApi request);
}
