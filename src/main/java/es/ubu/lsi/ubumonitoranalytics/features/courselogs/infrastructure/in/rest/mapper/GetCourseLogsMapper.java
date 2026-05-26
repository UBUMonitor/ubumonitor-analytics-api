package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.in.rest.mapper;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsInfoRequestDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsInfoResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.LogEntryDto;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info.FetchCourseLogsResult;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info.FetchLogLine;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info.CourseLogsInfoRequest;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(config = GlobalMapperConfig.class)
public interface GetCourseLogsMapper {

    CourseLogsInfoRequest toDomain(Integer courseId, CourseLogsInfoRequestDto courseLogsInfoRequestDto);



    @Mapping(target = "content", source = "logs")
    @Mapping(target = "page", source = "fetchCourseLogsResult")
    CourseLogsInfoResponseDto toDto(FetchCourseLogsResult fetchCourseLogsResult);

    @Mapping(target = "timeCreated", source = "timestamp")
    @Mapping(target = "ip", source = "ipAddress")
    @Mapping(target = "component", source = "componentName")
    LogEntryDto toLogEntryDto(FetchLogLine fetchLogLine);
}
