package es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.domain.model.UserCourses;

import java.util.List;


public interface UserCoursePersistencePort {
    void saveCourses(UserCourses userCourses);

    List<Course> getUserCourses(Integer userId);
}
