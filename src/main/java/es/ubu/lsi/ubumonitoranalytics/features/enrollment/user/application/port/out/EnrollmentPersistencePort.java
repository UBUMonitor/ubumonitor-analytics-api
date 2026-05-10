package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.UserEnrolledCourses;

import java.util.List;

public interface EnrollmentPersistencePort {
    void sync(Integer userId, List<Enrollment> enrolledCours);

    UserEnrolledCourses fetchUserEnrolledCourses(Integer userId);
}
