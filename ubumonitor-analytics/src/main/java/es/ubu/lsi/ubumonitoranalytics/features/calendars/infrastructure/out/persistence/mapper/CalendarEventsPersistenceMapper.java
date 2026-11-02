package es.ubu.lsi.ubumonitoranalytics.features.calendars.infrastructure.out.persistence.mapper;

import es.ubu.lsi.ubumonitoranalytics.features.calendars.domain.model.CalendarEvent;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.CalendarsEventsRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = GlobalMapperConfig.class)
public interface CalendarEventsPersistenceMapper {

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "groupId", source = "groupId", qualifiedByName = "positiveGroupId")
  void toRecord(CalendarEvent event, @MappingTarget CalendarsEventsRecord record);

  CalendarEvent toDomain(CalendarsEventsRecord record);

  @Named("positiveGroupId")
  default Long positiveGroupId(Long groupId) {
    return groupId != null && groupId > 0 ? groupId : null;
  }
}
