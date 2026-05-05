package es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.domain.model.CourseEnrollment;

public interface GetCourseEnrollmentsUseCase {

    CourseEnrollment getCourseEnrollment(Integer courseId);
}
