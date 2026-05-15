package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.UserGroupPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Group;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence.mapper.UserGroupMapper;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserGroupEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserGroupId;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserGroupPersistenceAdapter implements UserGroupPersistencePort {

    private final UserGroupRepository userGroupRepository;
    private final UserGroupMapper userGroupMapper;

    @Override
    public void syncUserGroups(
        List<Integer> userIds,
        List<User> users,
        Integer courseId
    ) {

        // 1. Traer existentes del curso + usuarios
        List<UserGroupEntity> existing = userGroupRepository
            .findByUserIdInAndCourseId(userIds, courseId);

        // 2. Index para lookup rápido
        Map<UserGroupId, UserGroupEntity> existingMap = existing.stream()
            .collect(Collectors.toMap(UserGroupEntity::getId, Function.identity()));

        existing.forEach(e -> e.setActive(false));

        List<UserGroupEntity> result = new ArrayList<>();

        // 4. Procesar nuevos datos
        for (User user : users) {
            for (Group group : user.getGroups()) {

                UserGroupId userGroupId = new UserGroupId(user.getId(), group.getId(), courseId);

                UserGroupEntity entity = existingMap.get(userGroupId);

                if (entity != null) {
                    entity.setActive(true);
                    result.add(entity);

                } else {
                    result.add(
                        userGroupMapper.toEntity(
                            user,
                            group,
                            courseId,
                            true
                        )
                    );
                }
            }
        }

        userGroupRepository.saveAll(result);
    }

}
