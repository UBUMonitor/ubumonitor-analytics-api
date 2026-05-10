package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out;


import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;

import java.util.Collection;
import java.util.List;

public interface UserPersistencePort {


    void syncUsers(Collection<User> users);
}
