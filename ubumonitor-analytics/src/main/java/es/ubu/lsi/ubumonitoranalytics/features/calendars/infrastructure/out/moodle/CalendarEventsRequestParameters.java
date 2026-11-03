package es.ubu.lsi.ubumonitoranalytics.features.calendars.infrastructure.out.moodle;

import java.util.List;

public record CalendarEventsRequestParameters(List<Long> courseIds) {}
