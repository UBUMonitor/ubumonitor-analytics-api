package es.ubu.lsi.ubumonitoranalytics.features.course.content.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.out.CourseContentPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseContent;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseModule;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.Section;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.ModuleEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.SectionEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.CourseRepository;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.ModuleRepository;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SyncCourseContentPersistenceAdapter implements CourseContentPersistencePort {

    private final CoursePersistenceMapper mapper;
    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;

    @Override
    public void save(CourseContent content) {

        Integer courseId = content.getCourseId();

        // 1. cargar estado actual desde BD
        List<SectionEntity> existingSections = sectionRepository.findByCourseIdWithModules(courseId);

        Map<Integer, SectionEntity> sectionDbMap = existingSections.stream()
            .filter(s -> s.getId() != null)
            .collect(Collectors.toMap(SectionEntity::getId, Function.identity()));

        Set<Integer> incomingSectionIds = new HashSet<>();

        // 2. sync sections
        List<SectionEntity> result = new ArrayList<>();

        for (Section sectionDomain : content.getSections()) {

            SectionEntity section = sectionDbMap.get(sectionDomain.getId());

            if (section == null) {
                section = mapper.toEntity(sectionDomain, courseId);
                section.setCourse(courseRepository.getReferenceById(courseId));
            } else {
                mapper.updateSectionFields(sectionDomain, section);
            }

            incomingSectionIds.add(section.getId());

            syncModules(section, sectionDomain.getModules());
            result.add(section);
        }

        // 3. desactivar secciones que ya no vienen
        for (SectionEntity dbSection : existingSections) {
            if (!incomingSectionIds.contains(dbSection.getId())) {
                dbSection.setActive(false);
                dbSection.getModules().forEach(m -> m.setActive(false));
                result.add(dbSection);
            }
        }

        sectionRepository.saveAll(result);
    }

    private void syncModules(SectionEntity section, List<CourseModule> incomingModules) {

        if (incomingModules == null) {
            return;
        }

        Map<Integer, ModuleEntity> dbMap = section.getModules().stream()
            .filter(m -> m.getId() != null)
            .collect(Collectors.toMap(ModuleEntity::getId, Function.identity()));

        Set<Integer> incomingIds = new HashSet<>();

        for (CourseModule dto : incomingModules) {

            ModuleEntity module = null;

            // 1. primero buscar en la sección actual
            if (dto.getId() != null) {
                module = dbMap.get(dto.getId());
            }

            // 2. si no está, buscar en BD
            if (module == null && dto.getId() != null) {
                module = moduleRepository.findById(dto.getId())
                    .orElse(null);
            }

            // 3. crear nuevo si realmente no existe
            if (module == null) {

                module = mapper.toEntity(dto);

                module.setSection(section);
                module.setActive(true);

                section.addModule(module);

            } else {

                mapper.updateModuleFields(dto, module);

                // asegurar relación correcta
                module.setSection(section);

                module.setActive(true);
            }

            incomingIds.add(module.getId());
        }

        // soft delete
        for (ModuleEntity dbModule : section.getModules()) {

            if (dbModule.getId() != null &&
                !incomingIds.contains(dbModule.getId())) {

                dbModule.setActive(false);
            }
        }
    }

}
