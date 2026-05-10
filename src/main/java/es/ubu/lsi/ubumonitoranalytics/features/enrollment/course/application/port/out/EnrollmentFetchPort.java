package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.CourseEnrollment;


public interface EnrollmentFetchPort {


    CourseEnrollment fetchCourseEnrolledUsers(Integer courseId, String token);
}

