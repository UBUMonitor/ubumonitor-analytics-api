package es.ubu.lsi.ubumonitoranalytics.features.grades.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.CourseGradesApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseGradeItemsResponseDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/** Implements the generated REST operations for Moodle course grade information. */
@Service
@RequiredArgsConstructor
public class CourseGradesApiDelegateImpl implements CourseGradesApiDelegate {

  private final CourseGradesMapper courseGradesMapper;

  @Override
  public ResponseEntity<CourseGradeItemsResponseDto> getCourseGradesInfo(
      Integer courseId, List<Integer> gradeItemIds, List<Integer> userIds) {
    return CourseGradesApiDelegate.super.getCourseGradesInfo(courseId, gradeItemIds, userIds);
  }

  @Override
  public ResponseEntity<CourseGradeItemsResponseDto> syncCourseGrades(Integer courseId) {
    return CourseGradesApiDelegate.super.syncCourseGrades(courseId);
  }
}
