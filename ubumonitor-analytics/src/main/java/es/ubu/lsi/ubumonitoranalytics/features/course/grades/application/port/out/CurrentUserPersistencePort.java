package es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.out;

public interface CurrentUserPersistencePort {
  Integer getUserId(String userName);
}
