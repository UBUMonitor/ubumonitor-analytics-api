package es.ubu.lsi.ubumonitoranalytics.features.course.content.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.in.SyncCourseContentUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.out.CourseContentPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.out.FetchCourseContentPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Fetches course content from Moodle and stores it for the current tenant. */
@Service
@RequiredArgsConstructor
public class SyncCourseContentService implements SyncCourseContentUseCase {

  private final CourseContentPersistencePort persistencePort;
  private final FetchCourseContentPort fetchCourseContentPort;

  /**
   * @param courseId Moodle course identifier
   * @return fetched course sections and modules
   */
  @Transactional
  @Override
  public CourseContent syncCourseContent(Integer courseId) {
    CourseContent content = fetchCourseContentPort.fetchCourseContent(courseId);
    persistencePort.save(content);

    return content;
  }
}
