package es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.domain.model.CourseEnrollment;

public interface EnrollmentGetPersistencePort {

    CourseEnrollment getCourseEnrollment(Integer courseId);
}
