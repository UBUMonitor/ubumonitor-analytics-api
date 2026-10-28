package es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.infrastructure.in;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.ActivityCompletionDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseActivityCompletionResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.UserActivityCompletionDto;
import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.domain.model.ActivityCompletion;
import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.domain.model.ActivityCompletionResult;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps course-content REST request and response models. */
@Mapper(config = GlobalMapperConfig.class)
public interface CourseActivityCompletionApiDelegateMapper {
  default CourseActivityCompletionResponseDto toResponseDto(ActivityCompletionResult result) {
    List<ActivityCompletionDto> activities =
        result.getActivityCompletions().stream()
            .collect(
                Collectors.groupingBy(
                    ActivityCompletion::getModuleId, LinkedHashMap::new, Collectors.toList()))
            .entrySet()
            .stream()
            .map(e -> toActivityDto(e.getKey(), e.getValue()))
            .toList();

    return new CourseActivityCompletionResponseDto(activities);
  }

  @Mapping(target = "moduleId", source = "moduleId")
  @Mapping(target = "users", source = "completions")
  ActivityCompletionDto toActivityDto(Long moduleId, List<ActivityCompletion> completions);

  UserActivityCompletionDto toUserDto(ActivityCompletion completion);
}
