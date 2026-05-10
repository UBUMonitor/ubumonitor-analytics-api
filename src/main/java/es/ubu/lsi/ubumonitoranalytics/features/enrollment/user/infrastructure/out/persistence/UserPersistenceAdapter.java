package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out.UserPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPersistencePort {
    private final UserRepository userRepository;

    @Override
    public Integer findOrCreate(Integer userId) {
         return userRepository.findById(userId)
             .map(UserEntity::getId)
             .orElseGet(() -> {
                UserEntity user = new UserEntity();
                user.setId(userId);
                userRepository.save(user);
                return userId;
            });
    }
}
