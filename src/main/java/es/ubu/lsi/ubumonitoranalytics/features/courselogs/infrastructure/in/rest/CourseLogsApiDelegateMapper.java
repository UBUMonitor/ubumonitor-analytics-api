package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.ImportCourseLogsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.ProcessLogsResult;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface CourseLogsApiDelegateMapper {
    ImportCourseLogsResponseDto toDto(ProcessLogsResult processLogsResult);
}
