package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollment;

public interface EnrollmentGetPersistencePort {

    CourseEnrollment getCourseEnrollment(Integer courseId);
}
