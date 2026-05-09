package es.ubu.lsi.ubumonitoranalytics.features.coursemodule.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.coursemodule.application.port.out.CourseContentPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.coursemodule.domain.model.CourseContent;
import es.ubu.lsi.ubumonitoranalytics.features.coursemodule.domain.model.CourseModule;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.ModuleEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.SectionEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.SectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CourseContentPersistenceAdapter implements CourseContentPersistencePort {

    private final CoursePersistenceMapper mapper;
    private final SectionRepository sectionRepository;

    @Override
    public void save(CourseContent content) {

        // 1. Convertir sections (sin relaciones aún)
        List<SectionEntity> sections = toSectionEntities(content);

        // 2. Indexar sections por id (para linking rápido)
        Map<Integer, SectionEntity> sectionMap = sections.stream()
                .collect(Collectors.toMap(SectionEntity::getId, s -> s));

        // 3. Construir y enlazar modules
        for (CourseModule module : content.getModules()) {

            ModuleEntity moduleEntity = mapper.toEntity(module);

            SectionEntity section = sectionMap.get(module.getSectionId());

            if (section == null) {
                throw new IllegalStateException(
                        "Section not found for module: " + module.getId()
                );
            }

            moduleEntity.setSection(section);
            section.getModules().add(moduleEntity);
        }

        // 4. Persistencia (cascade desde SectionEntity)
        sectionRepository.saveAll(sections);
    }

    private List<SectionEntity> toSectionEntities(CourseContent content) {

        if (content.getSections() == null) {
            return List.of();
        }

        List<SectionEntity> sections = content.getSections()
            .stream()
            .map(mapper::toEntity)
            .toList();

        Map<Integer, SectionEntity> sectionMap = sections.stream()
            .collect(Collectors.toMap(SectionEntity::getId, s -> s));

        for (CourseModule module : content.getModules()) {

            ModuleEntity moduleEntity = mapper.toEntity(module);

            SectionEntity section = sectionMap.get(module.getSectionId());

            if (section == null) {
                throw new IllegalStateException(
                    "Section not found for module " + module.getId()
                );
            }

            moduleEntity.setSection(section);

            if (section.getModules() == null) {
                section.setModules(new ArrayList<>());
            }

            section.getModules().add(moduleEntity);
        }

        return sections;
    }
}
