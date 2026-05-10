package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.CourseEnrollment;

public interface SyncCourseEnrollmentsUseCase {

    CourseEnrollment syncCourseEnrollments(Integer courseId);

}

