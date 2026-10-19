package es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.out;

public interface CurrentUserPersistencePort {
  Long getUserId(String userName);
}
