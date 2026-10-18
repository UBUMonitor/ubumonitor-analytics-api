package es.ubu.lsi.ubumonitoranalytics.features.grades.application.port.out;

public interface CurrentUserPersistencePort {
  Integer getUserId(String userName);
}
