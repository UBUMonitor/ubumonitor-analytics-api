package es.ubu.lsi.ubumonitoranalytics.features.course.assignments.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.mod.assign.getassignments.request.GetAssignmentsRequestApi;
import es.ubu.lsi.moodle.model.mod.assign.getassignments.response.AssignmentApi;
import es.ubu.lsi.moodle.model.mod.assign.getsubmissions.request.GetSubmissionsRequestApi;
import es.ubu.lsi.moodle.model.mod.assign.getsubmissions.response.SubmissionApi;
import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.domain.model.AssignmentSubmission;
import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.domain.model.CourseAssignment;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface CourseAssignmentSubmissionsMoodleMapper {

  @Mapping(target = "wsfunction", ignore = true)
  @Mapping(target = "args.courseids", source = "ids")
  GetAssignmentsRequestApi toAssignmentsRequest(IdsRequest<Long> request);

  @Mapping(target = "wsfunction", ignore = true)
  @Mapping(target = "args.assignmentids", source = "ids")
  GetSubmissionsRequestApi toSubmissionsRequest(IdsRequest<Integer> request);

  @Mapping(target = "id", source = "assignment.id")
  @Mapping(target = "courseId", source = "courseId")
  @Mapping(target = "moduleId", source = "assignment.cmid")
  @Mapping(target = "name", source = "assignment.name")
  @Mapping(target = "intro", source = "assignment.intro")
  @Mapping(target = "timeModified", source = "assignment.timemodified")
  @Mapping(target = "dueDate", source = "assignment.duedate")
  @Mapping(target = "submissionsFromDate", source = "assignment.allowsubmissionsfromdate")
  @Mapping(target = "cutoffDate", source = "assignment.cutoffdate")
  @Mapping(target = "gradingDueDate", source = "assignment.gradingduedate")
  CourseAssignment toDomain(AssignmentApi assignment, Long courseId);

  @Mapping(target = "submissionId", source = "submission.id")
  @Mapping(target = "assignmentId", source = "assignmentId")
  @Mapping(target = "courseId", source = "courseId")
  @Mapping(target = "userId", source = "userId")
  @Mapping(target = "attemptNumber", source = "submission.attemptnumber")
  @Mapping(target = "timeCreated", source = "submission.timecreated")
  @Mapping(target = "timeModified", source = "submission.timemodified")
  @Mapping(target = "timeStarted", source = "submission.timestarted")
  @Mapping(target = "status", source = "submission.status")
  @Mapping(target = "latest", source = "submission.latest")
  @Mapping(target = "gradingStatus", source = "submission.gradingstatus")
  AssignmentSubmission toDomain(
      SubmissionApi submission, Long courseId, Long assignmentId, Long userId);
}
