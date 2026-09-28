package es.ubu.lsi.ubumonitoranalytics.features.course.content.infrastructure.in;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseContentResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseContent;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface CourseContentApiDelegateMapper {

  @Mapping(target = "sections", source = "sections")
  CourseContentResponseDto toDto(CourseContent courseContent);
}
