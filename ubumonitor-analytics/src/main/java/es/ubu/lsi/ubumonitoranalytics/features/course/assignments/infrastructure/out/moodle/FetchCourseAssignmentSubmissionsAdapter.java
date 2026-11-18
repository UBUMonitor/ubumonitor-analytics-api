package es.ubu.lsi.ubumonitoranalytics.features.course.assignments.infrastructure.out.moodle;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.mod.assign.getassignments.response.AssignmentApi;
import es.ubu.lsi.moodle.model.mod.assign.getassignments.response.GetAssignmentsResponseApi;
import es.ubu.lsi.moodle.model.mod.assign.getsubmissions.response.AssignmentSubmissionsApi;
import es.ubu.lsi.moodle.model.mod.assign.getsubmissions.response.GetSubmissionsResponseApi;
import es.ubu.lsi.moodle.model.mod.assign.getsubmissions.response.SubmissionApi;
import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.application.port.out.FetchCourseAssignmentSubmissionsPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.application.port.out.FetchGroupMembersPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.domain.model.AssignmentSubmission;
import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.domain.model.CourseAssignment;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FetchCourseAssignmentSubmissionsAdapter
    implements FetchCourseAssignmentSubmissionsPort {

  private final Client client;
  private final CourseAssignmentSubmissionsMoodleMapper mapper;
  private final FetchGroupMembersPort fetchGroupMembersPort;

  @Override
  public List<CourseAssignment> fetchAssignments(Long courseId) {
    GetAssignmentsResponseApi assignmentsResponse =
        client
            .modAssign()
            .getAssignments(mapper.toAssignmentsRequest(new IdsRequest<>(List.of(courseId))));
    return createAssignments(courseId, assignmentsResponse);
  }

  @Override
  public List<AssignmentSubmission> fetchSubmissions(
      Long courseId, List<CourseAssignment> assignments) {
    List<Long> assignmentIds = assignments.stream().map(CourseAssignment::getId).toList();
    if (assignmentIds.isEmpty()) {
      return List.of();
    }
    List<Integer> moodleAssignmentIds = assignmentIds.stream().map(Math::toIntExact).toList();
    GetSubmissionsResponseApi submissionsResponse =
        client
            .modAssign()
            .getSubmissions(mapper.toSubmissionsRequest(new IdsRequest<>(moodleAssignmentIds)));
    if (submissionsResponse == null || submissionsResponse.getAssignments() == null) {
      return List.of();
    }

    Set<Long> groupIds = collectGroupIds(submissionsResponse.getAssignments());
    Map<Long, List<Long>> usersByGroupId = fetchGroupMembersPort.fetchGroupMembers(groupIds);
    List<AssignmentSubmission> result = new ArrayList<>();
    for (AssignmentSubmissionsApi assignmentSubmissions : submissionsResponse.getAssignments()) {
      if (assignmentSubmissions.getSubmissions() == null) {
        continue;
      }
      Long assignmentId = assignmentSubmissions.getAssignmentid().longValue();
      for (SubmissionApi submission : assignmentSubmissions.getSubmissions()) {
        addSubmission(result, courseId, assignmentId, submission, usersByGroupId);
      }
    }
    return List.copyOf(result);
  }

  private List<CourseAssignment> createAssignments(
      Long courseId, GetAssignmentsResponseApi assignmentsResponse) {
    List<CourseAssignment> assignments = new ArrayList<>();
    if (assignmentsResponse == null || assignmentsResponse.getCourses() == null) {
      return assignments;
    }
    for (var course : assignmentsResponse.getCourses()) {
      if (course.getId() == null
          || !courseId.equals(course.getId())
          || course.getAssignments() == null) {
        continue;
      }
      for (AssignmentApi assignment : course.getAssignments()) {
        if (assignment.getId() != null) {
          assignments.add(mapper.toDomain(assignment, courseId));
        }
      }
    }
    return List.copyOf(assignments);
  }

  private Set<Long> collectGroupIds(List<AssignmentSubmissionsApi> assignments) {
    Set<Long> groupIds = new LinkedHashSet<>();
    for (AssignmentSubmissionsApi assignment : assignments) {
      if (assignment.getSubmissions() == null) {
        continue;
      }
      for (SubmissionApi submission : assignment.getSubmissions()) {
        if (submission.getUserid() != null
            && submission.getUserid() == 0
            && submission.getGroupid() != null
            && submission.getGroupid() > 0) {
          groupIds.add(submission.getGroupid().longValue());
        }
      }
    }
    return groupIds;
  }

  private void addSubmission(
      List<AssignmentSubmission> result,
      Long courseId,
      Long assignmentId,
      SubmissionApi submission,
      Map<Long, List<Long>> usersByGroupId) {
    if (submission.getUserid() == null || submission.getId() == null) {
      return;
    }
    if (submission.getUserid() > 0) {
      result.add(
          mapper.toDomain(submission, courseId, assignmentId, submission.getUserid().longValue()));
      return;
    }
    if (submission.getUserid() == 0
        && submission.getGroupid() != null
        && submission.getGroupid() > 0) {
      List<Long> userIds =
          usersByGroupId.getOrDefault(submission.getGroupid().longValue(), List.of());
      for (Long userId : userIds) {
        if (userId != null && userId > 0) {
          result.add(mapper.toDomain(submission, courseId, assignmentId, userId));
        }
      }
    }
  }
}
