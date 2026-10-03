package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.in.rest.mapper;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.LogColumnComponentsEventsInnerDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.LogColumnDto;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info.CourseComponentEvent;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info.CourseLogsInfoResult;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps course log column metadata to REST response models. */
@Mapper(config = GlobalMapperConfig.class)
public interface ColumnInfoMapper {

  @Mapping(target = "stats", ignore = true)
  @Mapping(target = "column", source = "processLogsResult")
  CourseLogsResponseDto toDto(CourseLogsInfoResult processLogsResult);

  LogColumnDto toLogColumnDto(CourseLogsInfoResult processLogsResult);

  @Mapping(target = "component", source = "courseComponent")
  @Mapping(target = "event", source = "courseEvent")
  LogColumnComponentsEventsInnerDto toComponentsEventDto(CourseComponentEvent courseComponentEvent);
}
