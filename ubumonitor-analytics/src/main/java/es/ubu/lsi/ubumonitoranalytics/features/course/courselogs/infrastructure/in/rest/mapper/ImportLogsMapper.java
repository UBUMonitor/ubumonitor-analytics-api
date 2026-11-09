package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.in.rest.mapper;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.ProcessLogsResult;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps course log import responses to REST models. */
@Mapper(config = GlobalMapperConfig.class, uses = ColumnInfoMapper.class)
public interface ImportLogsMapper {
  @Mapping(target = "stats", source = "logImportStats")
  @Mapping(target = "column", source = "courseLogsInfoResult")
  CourseLogsResponseDto toDto(ProcessLogsResult processLogsResult);
}
