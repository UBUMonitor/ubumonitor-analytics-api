package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.in.GetCourseEnrollmentsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.out.LoadCourseEnrollmentsPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollments;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Retrieves persisted enrollment data for a course. */
@Service
@RequiredArgsConstructor
public class GetCourseEnrollmentsService implements GetCourseEnrollmentsUseCase {

  private final LoadCourseEnrollmentsPort loadCourseEnrollmentsPort;

  @Override
  @Transactional(readOnly = true)
  public CourseEnrollments getCourseEnrollments(Long courseId) {
    return loadCourseEnrollmentsPort.load(courseId);
  }
}
