package es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.application.port.out;



import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.domain.model.UserCourses;


public interface EnrollmentCoursesFetchPort {
    UserCourses fetchEnrolledCourses(Integer userId);

}
