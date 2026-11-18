package es.ubu.lsi.ubumonitoranalytics.features.course.assignments.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseAssignmentDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseAssignmentSubmissionDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseAssignmentSubmissionsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseAssignmentsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.domain.model.AssignmentSubmission;
import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.domain.model.CourseAssignment;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface CourseAssignmentSubmissionsApiDelegateMapper {

  default CourseAssignmentsResponseDto toAssignmentsResponse(List<CourseAssignment> assignments) {
    CourseAssignmentsResponseDto response = new CourseAssignmentsResponseDto();
    response.setAssignments(toAssignmentDtos(assignments));
    return response;
  }

  default CourseAssignmentSubmissionsResponseDto toSubmissionsResponse(
      List<AssignmentSubmission> submissions) {
    CourseAssignmentSubmissionsResponseDto response = new CourseAssignmentSubmissionsResponseDto();
    response.setSubmissions(toDtos(submissions));
    return response;
  }

  List<CourseAssignmentDto> toAssignmentDtos(List<CourseAssignment> assignments);

  CourseAssignmentDto toAssignmentDto(CourseAssignment assignment);

  List<CourseAssignmentSubmissionDto> toDtos(List<AssignmentSubmission> submissions);

  CourseAssignmentSubmissionDto toDto(AssignmentSubmission submission);
}
