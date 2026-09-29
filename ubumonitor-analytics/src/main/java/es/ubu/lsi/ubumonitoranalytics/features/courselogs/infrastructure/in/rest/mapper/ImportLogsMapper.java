package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.in.rest.mapper;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.LogColumnComponentsEventsInnerDto;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.importlogs.ProcessLogsResult;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info.CourseComponentEvent;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface ImportLogsMapper {
  @Mapping(target = "stats", source = "logImportStats")
  @Mapping(target = "column", source = "courseLogsInfoResult")
  CourseLogsResponseDto toDto(ProcessLogsResult processLogsResult);

  @Mapping(target = "component", source = "courseComponent")
  @Mapping(target = "event", source = "courseEvent")
  LogColumnComponentsEventsInnerDto toComponentsEventDto(CourseComponentEvent courseComponentEvent);
}
