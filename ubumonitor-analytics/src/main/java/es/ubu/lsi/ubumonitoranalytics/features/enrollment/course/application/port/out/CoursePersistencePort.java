package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Course;
import java.util.Collection;

/** Persists course records received during enrollment synchronization. */
public interface CoursePersistencePort {

  /**
   * @param courses courses to synchronize
   */
  void syncCourses(Collection<Course> courses);
}
