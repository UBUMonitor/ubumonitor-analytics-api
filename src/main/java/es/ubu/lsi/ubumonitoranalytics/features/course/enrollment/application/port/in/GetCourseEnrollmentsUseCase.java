package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollment;

public interface GetCourseEnrollmentsUseCase {

    CourseEnrollment getCourseEnrollment(Integer courseId);
}
