package es.ubu.lsi.ubumonitoranalytics.features.course.content.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.core.course.getcontents.request.GetCourseContentsRequestApi;
import es.ubu.lsi.moodle.model.core.course.getcontents.response.GetCourseContentsResponseApi;
import es.ubu.lsi.moodle.model.core.course.getcontents.response.ModuleApi;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseContent;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseModule;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.Section;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.MapperUtils;
import java.util.List;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = GlobalMapperConfig.class)
public interface FetchCourseContentAdapaterMapper {

  @Mapping(target = "wsfunction", ignore = true)
  @Mapping(target = "options", ignore = true)
  @Mapping(target = "courseid", source = "courseId")
  GetCourseContentsRequestApi toRequest(Integer courseId);

  @Mapping(target = "courseId", source = "courseId")
  @Mapping(target = "sections", source = "contents")
  CourseContent toDomain(List<GetCourseContentsResponseApi> contents, Integer courseId);

  @Mapping(target = "id", source = "id")
  @Mapping(target = "position", source = "section")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "summary", source = "content", qualifiedByName = "summarytext")
  @Mapping(target = "visible", source = "visible")
  @Mapping(target = "modules", source = "modules")
  Section toDomain(GetCourseContentsResponseApi content);

  @Mapping(target = "id", source = "id")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "modName", source = "modname")
  @Mapping(target = "url", source = "url")
  @Mapping(target = "description", source = "moduleApi", qualifiedByName = "descriptiontext")
  @Mapping(target = "visible", source = "visible")
  CourseModule toDomain(ModuleApi moduleApi);

  @Named("summarytext")
  default String toSummaryText(GetCourseContentsResponseApi source) {
    return MapperUtils.parseMoodleContent(
        source.getSummary(),
        Optional.ofNullable(source.getSummaryformat())
            .map(GetCourseContentsResponseApi.Summaryformat::value)
            .orElse(null));
  }

  @Named("descriptiontext")
  default String toDescriptionText(ModuleApi moduleApi) {
    return MapperUtils.parseMoodleContent(moduleApi.getDescription(), 1);
  }
}
