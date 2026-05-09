package es.ubu.lsi.ubumonitoranalytics.features.coursemodule.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.coursemodule.domain.model.CourseContent;
import es.ubu.lsi.ubumonitoranalytics.features.coursemodule.domain.model.CourseModule;
import es.ubu.lsi.ubumonitoranalytics.features.coursemodule.domain.model.Section;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.ModuleEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.entities.SectionEntity;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(config = GlobalMapperConfig.class)
public interface CoursePersistenceMapper {

    // =========================
    // SECTION
    // =========================
    @Mapping(target = "modules", ignore = true)
    SectionEntity toEntity(Section section);

    // =========================
    // MODULE
    // =========================
    @Mapping(target = "section", ignore = true)
    ModuleEntity toEntity(CourseModule module);


}
