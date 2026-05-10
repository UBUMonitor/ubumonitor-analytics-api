package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.UserRolePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence.mapper.UserRoleMapper;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserRoleEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserRolePersistenceAdapter implements UserRolePersistencePort {

    private final UserRoleRepository userRoleRepository;
    private final UserRoleMapper userRoleMapper;

    @Override
    public void syncUserRoles(
        List<Integer> userIds,
        List<User> users,
        Integer courseId
    ) {

        userRoleRepository.deactivateByUserIds(userIds);

        List<UserRoleEntity> entities = users.stream()
            .map(user ->
                userRoleMapper.toEntities(
                    user,
                    user.getRoles(),
                    courseId,
                    true
                )
            )
            .flatMap(List::stream)
            .toList();

        userRoleRepository.saveAll(entities);
    }
}
