package es.ubu.lsi.ubumonitoranalytics.features.course.forums.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.course.forums.application.port.in.GetCourseForumsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.application.port.out.LoadCourseForumsPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.CourseForums;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCourseForumsService implements GetCourseForumsUseCase {

  private final LoadCourseForumsPort loadCourseForumsPort;

  @Override
  @Transactional(readOnly = true)
  public CourseForums getCourseForums(Long courseId) {
    return loadCourseForumsPort.load(courseId);
  }
}
