package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.in.rest.mapper;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsListRequestDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsListResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.LogEntryDto;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.list.CourseLogsInfoRequest;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.list.FetchCourseLogsResult;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.list.FetchLogLine;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps course log list requests between REST and application models. */
@Mapper(config = GlobalMapperConfig.class)
public interface GetCourseLogsMapper {

  CourseLogsInfoRequest toDomain(
      Integer courseId, CourseLogsListRequestDto courseLogsInfoRequestDto);

  @Mapping(target = "content", source = "logs")
  @Mapping(target = "page", source = "fetchCourseLogsResult")
  CourseLogsListResponseDto toDto(FetchCourseLogsResult fetchCourseLogsResult);

  @Mapping(target = "timeCreated", source = "timestamp")
  @Mapping(target = "ip", source = "ipAddress")
  @Mapping(target = "component", source = "componentName")
  LogEntryDto toLogEntryDto(FetchLogLine fetchLogLine);
}
