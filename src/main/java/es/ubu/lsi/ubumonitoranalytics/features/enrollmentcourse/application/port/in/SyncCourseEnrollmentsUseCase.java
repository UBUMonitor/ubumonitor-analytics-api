package es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.domain.model.CourseEnrollment;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.domain.model.Enrollment;

import java.util.List;

public interface SyncCourseEnrollmentsUseCase {

    CourseEnrollment syncCourseEnrollments(Integer courseId);

}
