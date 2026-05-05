package es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.domain.model.UserCourses;

public interface GetUserCoursesUseCase {
    UserCourses getActualUserEnrollments();
}
