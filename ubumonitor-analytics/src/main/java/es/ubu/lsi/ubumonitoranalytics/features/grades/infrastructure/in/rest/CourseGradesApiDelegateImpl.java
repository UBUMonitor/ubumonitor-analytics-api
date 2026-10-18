package es.ubu.lsi.ubumonitoranalytics.features.grades.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.CourseGradesApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseGradeItemsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.grades.application.port.in.GradesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/** Implements the generated REST operations for Moodle course grade information. */
@Service
@RequiredArgsConstructor
public class CourseGradesApiDelegateImpl implements CourseGradesApiDelegate {

  private final CourseGradesMapper courseGradesMapper;
  private final GradesUseCase gradesUseCase;

  @Override
  public ResponseEntity<CourseGradeItemsResponseDto> getCourseGradesInfo(Integer courseId) {
    return ResponseEntity.ok(courseGradesMapper.toResponseDto(gradesUseCase.get(courseId)));
  }

  @Override
  public ResponseEntity<CourseGradeItemsResponseDto> syncCourseGrades(Integer courseId) {
    return CourseGradesApiDelegate.super.syncCourseGrades(courseId);
  }
}
