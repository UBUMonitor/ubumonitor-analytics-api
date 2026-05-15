package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.UserRolePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Role;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence.mapper.UserRoleMapper;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserRoleEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserRoleId;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserRolePersistenceAdapter implements UserRolePersistencePort {

    private final UserRoleRepository userRoleRepository;
    private final UserRoleMapper userRoleMapper;

    @Override
    public void syncUserRoles(List<Integer> userIds, List<User> users, Integer courseId) {

        List<UserRoleEntity> existing = userRoleRepository
            .findByUserIdInAndCourseId(userIds, courseId);

        Map<UserRoleId, UserRoleEntity> existingMap = existing.stream()
            .collect(Collectors.toMap(
                UserRoleEntity::getId,
                Function.identity()
            ));

        existing.forEach(e -> e.setActive(false));

        List<UserRoleEntity> result = new ArrayList<>();

        for (User user : users) {
            for (Role role : user.getRoles()) {

                UserRoleId id = new UserRoleId(
                    user.getId(),
                    role.getId(),
                    courseId
                );

                UserRoleEntity entity = existingMap.get(id);

                if (entity != null) {
                    entity.setActive(true);
                    result.add(entity);

                } else {
                    result.add(
                        userRoleMapper.toEntity(
                            user,
                            role,
                            courseId,
                            true
                        )
                    );
                }
            }
        }

        // 5. Persistir cambios
        userRoleRepository.saveAll(result);
    }
}
