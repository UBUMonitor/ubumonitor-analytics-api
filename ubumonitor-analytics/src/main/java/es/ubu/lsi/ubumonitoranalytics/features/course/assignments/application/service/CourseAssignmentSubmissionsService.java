package es.ubu.lsi.ubumonitoranalytics.features.course.assignments.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.application.port.in.CourseAssignmentSubmissionsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.application.port.out.CourseAssignmentSubmissionsPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.application.port.out.FetchCourseAssignmentSubmissionsPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.domain.model.AssignmentSubmission;
import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.domain.model.CourseAssignment;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseAssignmentSubmissionsService implements CourseAssignmentSubmissionsUseCase {

  private final FetchCourseAssignmentSubmissionsPort fetchPort;
  private final CourseAssignmentSubmissionsPersistencePort persistencePort;

  @Override
  public List<CourseAssignment> sync(Long courseId) {
    List<CourseAssignment> assignments = fetchPort.fetchAssignments(courseId);
    persistencePort.syncAssignments(courseId, assignments);

    List<AssignmentSubmission> submissions = fetchPort.fetchSubmissions(courseId, assignments);
    persistencePort.syncSubmissions(courseId, submissions);
    return assignments;
  }

  @Override
  @Transactional(readOnly = true)
  public List<CourseAssignment> get(Long courseId) {
    return persistencePort.getAssignments(courseId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AssignmentSubmission> get(
      Long courseId, List<Long> assignmentIds, List<Long> userIds) {
    return persistencePort.get(courseId, assignmentIds, userIds);
  }
}
