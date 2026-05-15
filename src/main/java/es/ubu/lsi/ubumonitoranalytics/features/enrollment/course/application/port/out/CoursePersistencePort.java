package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Course;

import java.util.Collection;

public interface CoursePersistencePort {

    void syncCourses(Collection<Course> courses);
}
