package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.UserPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence.mapper.UserMapper;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EnrollmentUserPersistenceAdapter implements UserPersistencePort {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public void syncUsers(Collection<User> users) {

        if (users.isEmpty()) {
            return;
        }

        Set<Integer> ids = users.stream()
            .map(User::getId)
            .collect(Collectors.toSet());

        Map<Integer, UserEntity> existing = userRepository.findAllById(ids)
            .stream()
            .collect(Collectors.toMap(UserEntity::getId, u -> u));

        List<UserEntity> entities = new ArrayList<>();

        for (User user : users) {

            UserEntity entity =
                existing.getOrDefault(user.getId(), new UserEntity());

            userMapper.toEntity(user, entity);

            entities.add(entity);
        }

        userRepository.saveAll(entities);
    }
}
