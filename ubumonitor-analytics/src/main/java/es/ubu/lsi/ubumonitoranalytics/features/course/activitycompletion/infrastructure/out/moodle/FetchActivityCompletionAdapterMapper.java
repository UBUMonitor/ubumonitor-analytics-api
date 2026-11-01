package es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.core.completion.getactivitiescompletionstatus.request.GetActivitiesCompletionStatusRequestApi;
import es.ubu.lsi.moodle.model.core.completion.getactivitiescompletionstatus.response.ActivityCompletionStatusApi;
import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.domain.model.ActivityCompletion;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.util.List;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps Moodle course-content responses to application models. */
@Mapper(config = GlobalMapperConfig.class)
public interface FetchActivityCompletionAdapterMapper {
  @Mapping(target = "args.courseid", source = "courseId")
  @Mapping(target = "args.userid", source = "userId")
  @Mapping(target = "wsfunction", ignore = true)
  GetActivitiesCompletionStatusRequestApi toRequest(Long courseId, Long userId);

  List<ActivityCompletion> toResponse(
      List<ActivityCompletionStatusApi> statusApis, @Context Long userId);

  @Mapping(target = "valueUsed", source = "valueused")
  @Mapping(target = "userVisible", source = "uservisible")
  @Mapping(target = "userId", expression = "java(userId)")
  @Mapping(target = "timeCompleted", source = "timecompleted")
  @Mapping(target = "overrideBy", source = "overrideby")
  @Mapping(target = "moduleId", source = "cmid")
  @Mapping(target = "isTrackedUser", source = "istrackeduser")
  ActivityCompletion toResponse(ActivityCompletionStatusApi status, @Context Long userId);
}
