package es.ubu.lsi.ubumonitoranalytics.features.course.forums.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.course.forums.application.port.in.SyncCourseForumsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.application.port.out.FetchCourseForumsPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.application.port.out.SaveCourseForumsPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.CourseForums;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncCourseForumsService implements SyncCourseForumsUseCase {

  private final FetchCourseForumsPort fetchCourseForumsPort;
  private final SaveCourseForumsPort saveCourseForumsPort;

  @Override
  public CourseForums syncCourseForums(Long courseId) {
    CourseForums courseForums = fetchCourseForumsPort.fetchCourseForums(courseId);
    saveCourseForumsPort.save(courseForums);
    return courseForums;
  }
}
