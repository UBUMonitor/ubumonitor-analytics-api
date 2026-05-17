package es.ubu.lsi.ubumonitoranalytics.features.course.content.infrastructure.out.persistence;


import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseModule;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.Section;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.ModulesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.SectionsRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(config = GlobalMapperConfig.class)
public interface CourseContentPersistenceMapper {

    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "courseId", ignore = true)
    @Mapping(target = "active", ignore = true)
    void toRecord(Section section, @MappingTarget SectionsRecord sectionRecord);

    @Mapping(target = "modules", ignore = true)
    Section toDomain(SectionsRecord sectionsRecord);



    @Mapping(target = "sectionId", ignore = true)
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    void toRecord(CourseModule module, @MappingTarget ModulesRecord moduleRecord);


    CourseModule toDomain(ModulesRecord modulesRecord);
}
