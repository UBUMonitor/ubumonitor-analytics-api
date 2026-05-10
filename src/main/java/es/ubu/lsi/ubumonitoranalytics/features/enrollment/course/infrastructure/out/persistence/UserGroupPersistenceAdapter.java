package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.UserGroupPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence.mapper.UserGroupMapper;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.UserGroupEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

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

        userGroupRepository.deactivateByUserIds(userIds);

        List<UserGroupEntity> entities = users.stream()
            .map(user ->
                userGroupMapper.toEntities(
                    user,
                    user.getGroups(),
                    courseId,
                    true
                )
            )
            .flatMap(List::stream)
            .toList();

        userGroupRepository.saveAll(entities);
    }
}
