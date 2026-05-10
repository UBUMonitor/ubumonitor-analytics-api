package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.in;



import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.UserEnrolledCourses;


public interface SyncUserCoursesUseCase {
    UserEnrolledCourses syncActualUserEnrollments();
}

