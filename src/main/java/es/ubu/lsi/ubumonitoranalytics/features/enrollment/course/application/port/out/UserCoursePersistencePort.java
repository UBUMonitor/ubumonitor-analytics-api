package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.CourseEnrollment;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Enrollment;

import java.util.List;

public interface UserCoursePersistencePort {

    void syncUserCourses(List<Integer> userIds, List<Enrollment> enrollments, Integer courseId);
}
