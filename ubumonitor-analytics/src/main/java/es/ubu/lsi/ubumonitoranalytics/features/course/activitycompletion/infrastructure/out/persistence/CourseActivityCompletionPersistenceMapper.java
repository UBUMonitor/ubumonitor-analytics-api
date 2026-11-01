package es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.domain.model.ActivityCompletion;
import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.domain.model.ActivityCompletionResult;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersActivitiesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.time.OffsetDateTime;
import java.util.List;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps course-content records to and from domain models. */
@Mapper(config = GlobalMapperConfig.class)
public interface CourseActivityCompletionPersistenceMapper {

  @Mapping(target = "active", constant = "true")
  @Mapping(target = "updatedAt", expression = "java(offsetDateTime)")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "courseId", expression = "java(courseId)")
  UsersActivitiesRecord toRecord(
      ActivityCompletion activityCompletionResult,
      @Context Long courseId,
      @Context OffsetDateTime offsetDateTime);

  List<UsersActivitiesRecord> toRecords(
      List<ActivityCompletion> activityCompletionResult,
      @Context Long courseId,
      @Context OffsetDateTime now);

  @Mapping(target = "courseId", source = "courseId")
  @Mapping(target = "activityCompletions", source = "usersActivitiesRecords")
  ActivityCompletionResult toDomain(
      List<UsersActivitiesRecord> usersActivitiesRecords, Long courseId);

  @Mapping(target = "userId", source = "userId")
  @Mapping(target = "isTrackedUser", source = "isTrackedUser")
  @Mapping(target = "moduleId", source = "moduleId")
  @Mapping(target = "overrideBy", source = "overrideBy")
  @Mapping(target = "state", source = "state")
  @Mapping(target = "timeCompleted", source = "timeCompleted")
  @Mapping(target = "tracking", source = "tracking")
  @Mapping(target = "userVisible", source = "userVisible")
  @Mapping(target = "valueUsed", source = "valueUsed")
  ActivityCompletion toDomain(UsersActivitiesRecord usersActivitiesRecord);
}
