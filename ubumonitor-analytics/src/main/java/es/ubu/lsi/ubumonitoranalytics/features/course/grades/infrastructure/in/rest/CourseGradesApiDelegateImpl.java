package es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.CourseGradesApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseGradeItemsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseGradesValuesResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.in.GradesUseCase;
import java.util.List;
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
  public ResponseEntity<CourseGradeItemsResponseDto> getCourseGradesInfo(Long courseId) {
    return ResponseEntity.ok(
        courseGradesMapper.toCourseGradeItemsResponseDto(
            gradesUseCase.getGradeItems(courseId), courseId));
  }

  @Override
  public ResponseEntity<CourseGradeItemsResponseDto> syncCourseGrades(Long courseId) {
    return ResponseEntity.ok(
        courseGradesMapper.toCourseGradeItemsResponseDto(gradesUseCase.sync(courseId), courseId));
  }

  @Override
  public ResponseEntity<CourseGradesValuesResponseDto> getCourseGradesValues(
      Long courseId, List<Long> gradeItemsIds, List<Long> userIds) {
    return ResponseEntity.ok(
        courseGradesMapper.toCourseGradesValuesResponseDto(
            gradesUseCase.getUserGrades(courseId, gradeItemsIds, userIds), courseId));
  }
}
