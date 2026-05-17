package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.UsersResponse;

public interface EnrollmentGetPersistencePort {

    UsersResponse getCourseEnrollment(Integer courseId);
}

