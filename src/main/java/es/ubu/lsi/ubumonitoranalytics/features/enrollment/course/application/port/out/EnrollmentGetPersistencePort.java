package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.CourseEnrollment;

public interface EnrollmentGetPersistencePort {

    CourseEnrollment getCourseEnrollment(Integer courseId);
}

