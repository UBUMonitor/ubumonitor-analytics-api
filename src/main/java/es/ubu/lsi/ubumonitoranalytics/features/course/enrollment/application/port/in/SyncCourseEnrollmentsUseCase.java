package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollment;

public interface SyncCourseEnrollmentsUseCase {

    CourseEnrollment syncCourseEnrollments(Integer courseId);

}
