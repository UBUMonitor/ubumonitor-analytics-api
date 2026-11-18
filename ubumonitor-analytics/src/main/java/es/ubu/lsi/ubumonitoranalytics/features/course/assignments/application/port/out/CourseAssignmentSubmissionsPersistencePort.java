package es.ubu.lsi.ubumonitoranalytics.features.course.assignments.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.domain.model.AssignmentSubmission;
import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.domain.model.CourseAssignment;
import java.util.List;

public interface CourseAssignmentSubmissionsPersistencePort {
  void syncAssignments(Long courseId, List<CourseAssignment> assignments);

  void syncSubmissions(Long courseId, List<AssignmentSubmission> submissions);

  List<CourseAssignment> getAssignments(Long courseId);

  List<AssignmentSubmission> get(Long courseId, List<Long> assignmentIds, List<Long> userIds);
}
