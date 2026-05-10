package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out;


public interface UserPersistencePort {
    Integer findOrCreate(Integer userId);
}
