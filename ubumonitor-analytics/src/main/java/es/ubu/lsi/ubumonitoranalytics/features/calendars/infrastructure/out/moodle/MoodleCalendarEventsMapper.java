package es.ubu.lsi.ubumonitoranalytics.features.calendars.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.core.calendar.getcalendarevents.request.GetCalendarEventsRequestApi;
import es.ubu.lsi.moodle.model.core.calendar.getcalendarevents.response.EventApi;
import es.ubu.lsi.ubumonitoranalytics.features.calendars.domain.model.CalendarEvent;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.MapperUtils;
import java.util.List;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = GlobalMapperConfig.class)
public interface MoodleCalendarEventsMapper {

  @Mapping(target = "wsfunction", ignore = true)
  @Mapping(target = "args.options.userevents", constant = "1")
  @Mapping(target = "args.options.siteevents", constant = "1")
  @Mapping(target = "args.options.timestart", constant = "0L")
  @Mapping(target = "args.options.timeend", constant = "0L")
  @Mapping(target = "args.options.ignorehidden", constant = "0")
  @Mapping(target = "args.events.courseids", source = "courseIds")
  GetCalendarEventsRequestApi toRequest(CalendarEventsRequestParameters parameters);

  List<CalendarEvent> toDomain(List<EventApi> events);

  @Mapping(target = "courseId", source = "courseid")
  @Mapping(target = "categoryId", source = "categoryid")
  @Mapping(target = "groupId", source = "groupid")
  @Mapping(target = "userId", source = "userid")
  @Mapping(target = "repeatId", source = "repeatid")
  @Mapping(target = "moduleName", source = "modulename")
  @Mapping(target = "eventType", source = "eventtype")
  @Mapping(target = "timeStart", source = "timestart")
  @Mapping(target = "timeDuration", source = "timeduration")
  @Mapping(target = "timeModified", source = "timemodified")
  @Mapping(target = "subscriptionId", source = "subscriptionid")
  @Mapping(target = "description", source = "event", qualifiedByName = "parseDescription")
  @Mapping(target = "uuid", source = "uuid", qualifiedByName = "uuidValue")
  CalendarEvent toDomain(EventApi event);

  @Named("parseDescription")
  default String parseDescription(EventApi event) {
    Integer format = event.getFormat() == null ? null : event.getFormat().value();
    return MapperUtils.parseMoodleContent(event.getDescription(), format);
  }

  @Named("uuidValue")
  default String uuidValue(UUID uuid) {
    return uuid == null ? null : uuid.toString();
  }
}
