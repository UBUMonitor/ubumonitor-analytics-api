package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.in.rest.mapper;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.importlogs.ProcessLogsResult;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface ImportLogsMapper {
  CourseLogsResponseDto toDto(ProcessLogsResult processLogsResult);
}
