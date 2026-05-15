package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.CourseEnrollment;

public interface GetCourseEnrollmentsUseCase {

    CourseEnrollment getCourseEnrollment(Integer courseId);
}

