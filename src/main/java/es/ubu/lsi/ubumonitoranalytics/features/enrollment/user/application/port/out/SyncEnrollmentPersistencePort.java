package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.UserEnrolledCourses;

public interface SyncEnrollmentPersistencePort {


    void sync(UserEnrolledCourses userEnrolledCourses);
}
