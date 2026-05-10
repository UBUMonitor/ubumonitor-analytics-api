package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.CourseEnrollment;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.Course;

import java.util.List;

public interface CoursePersistencePort {
    void saveAll(List<Course> courses);

}
