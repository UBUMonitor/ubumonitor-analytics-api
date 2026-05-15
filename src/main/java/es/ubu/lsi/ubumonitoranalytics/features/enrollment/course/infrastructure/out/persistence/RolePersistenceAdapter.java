package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.RolePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.Role;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.persistence.mapper.RoleMapper;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.RoleEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RolePersistenceAdapter implements RolePersistencePort {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    public void syncRoles(Collection<Role> roles) {

        if (roles.isEmpty()) {
            return;
        }

        Map<Integer, Role> rolesById = roles.stream()
            .collect(Collectors.toMap(Role::getId, r -> r));

        List<RoleEntity> existing =
            roleRepository.findAllById(rolesById.keySet());

        Set<Integer> existingIds = existing.stream()
            .map(RoleEntity::getId)
            .collect(Collectors.toSet());

        for (RoleEntity entity : existing) {
            roleMapper.updateRoleEntity(
                rolesById.get(entity.getId()),
                entity
            );
        }

        List<RoleEntity> toCreate = roles.stream()
            .filter(r -> !existingIds.contains(r.getId()))
            .map(roleMapper::createRoleEntity)
            .toList();

        if (!toCreate.isEmpty()) {
            roleRepository.saveAll(toCreate);
        }
    }
}
