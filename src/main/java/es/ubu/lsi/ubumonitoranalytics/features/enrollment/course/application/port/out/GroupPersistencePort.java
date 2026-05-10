package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.CourseEnrollment;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Group;

import java.util.Collection;
import java.util.List;

public interface GroupPersistencePort {

    void syncGroups(Collection<Group> groups);
}
