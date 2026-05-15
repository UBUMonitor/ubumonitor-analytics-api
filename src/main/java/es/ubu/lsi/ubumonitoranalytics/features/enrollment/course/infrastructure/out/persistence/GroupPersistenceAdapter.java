package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.GroupPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Group;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence.mapper.GroupMapper;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.GroupEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GroupPersistenceAdapter implements GroupPersistencePort {

    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;

    @Override
    public void syncGroups(Collection<Group> groups) {

        if (groups.isEmpty()) {
            return;
        }

        Map<Integer, Group> groupsById = groups.stream()
            .collect(Collectors.toMap(Group::getId, g -> g));

        List<GroupEntity> existing =
            groupRepository.findAllById(groupsById.keySet());

        Set<Integer> existingIds = existing.stream()
            .map(GroupEntity::getId)
            .collect(Collectors.toSet());

        for (GroupEntity entity : existing) {
            groupMapper.updateGroupEntity(
                groupsById.get(entity.getId()),
                entity
            );
        }

        List<GroupEntity> toCreate = groups.stream()
            .filter(g -> !existingIds.contains(g.getId()))
            .map(groupMapper::createGroupEntity)
            .toList();

        if (!toCreate.isEmpty()) {
            groupRepository.saveAll(toCreate);
        }
    }
}
