package es.ubu.lsi.ubumonitoranalytics.features.course.content.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseContent;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseModule;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.Section;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.CourseEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.ModuleEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.SectionEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(config = GlobalMapperConfig.class)
public interface CoursePersistenceMapper {

    // =========================
    // SECTION
    // =========================
    @Mapping(target = "course.id", expression = "java(courseId)")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "modules", ignore = true)
    SectionEntity toEntity(Section section, @Context Integer courseId);

    // =========================
    // MODULE
    // =========================
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "section", ignore = true)
    ModuleEntity toEntity(CourseModule module);


    @Mapping(target = "course", ignore = true)
    @Mapping(target = "active", constant = "true")
    void updateSectionFields(Section section, @MappingTarget SectionEntity sectionEntity);

    @Mapping(target = "position", ignore = true)
    @Mapping(target = "section", ignore = true)
    @Mapping(target = "active", constant = "true")
    void updateModuleFields(CourseModule courseModule, @MappingTarget ModuleEntity moduleEntity);

    @Mapping(target = "courseId", source = "id")
    CourseContent toDomain(CourseEntity courseEntity);
}
