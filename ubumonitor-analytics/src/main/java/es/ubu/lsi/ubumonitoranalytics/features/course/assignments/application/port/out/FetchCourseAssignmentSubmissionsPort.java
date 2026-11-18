package es.ubu.lsi.ubumonitoranalytics.features.course.assignments.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.domain.model.AssignmentSubmission;
import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.domain.model.CourseAssignment;
import java.util.List;

public interface FetchCourseAssignmentSubmissionsPort {
  List<CourseAssignment> fetchAssignments(Long courseId);

  List<AssignmentSubmission> fetchSubmissions(Long courseId, List<CourseAssignment> assignments);
}
