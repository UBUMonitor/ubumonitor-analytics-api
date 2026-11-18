package es.ubu.lsi.ubumonitoranalytics.features.course.assignments.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.domain.model.AssignmentSubmission;
import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.domain.model.CourseAssignment;
import java.util.List;

public interface CourseAssignmentSubmissionsUseCase {
  List<CourseAssignment> sync(Long courseId);

  List<CourseAssignment> get(Long courseId);

  List<AssignmentSubmission> get(Long courseId, List<Long> assignmentIds, List<Long> userIds);
}
