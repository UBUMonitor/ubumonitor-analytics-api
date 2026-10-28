package es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersCourses.USERS_COURSES;

import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.application.port.out.CourseUsersPort;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseUsersPersistenceAdapter implements CourseUsersPort {
  private final Jooq jooq;

  @Override
  public List<Long> getCourseUsersIds(Long courseId) {
    return jooq.dsl()
        .select(USERS_COURSES.USER_ID)
        .from(USERS_COURSES)
        .where(USERS_COURSES.COURSE_ID.eq(courseId).and(USERS_COURSES.ACTIVE.isTrue()))
        .fetch(USERS_COURSES.USER_ID);
  }
}
