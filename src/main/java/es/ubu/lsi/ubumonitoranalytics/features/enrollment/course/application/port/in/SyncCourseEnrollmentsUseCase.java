package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.UsersResponse;

public interface SyncCourseEnrollmentsUseCase {

  UsersResponse syncCourseEnrollments(Integer courseId);
}
