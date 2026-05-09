package es.ubu.lsi.ubumonitoranalytics.features.coursemodule.infrastructure.out.moodle;


import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterCourseContentDto;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterCourseContentsResponseDto;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterModuleDto;
import es.ubu.lsi.ubumonitoranalytics.features.coursemodule.domain.model.CourseContent;
import es.ubu.lsi.ubumonitoranalytics.features.coursemodule.domain.model.CourseModule;
import es.ubu.lsi.ubumonitoranalytics.features.coursemodule.domain.model.Section;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;

@Mapper(config = GlobalMapperConfig.class)
public interface FetchCourseContentAdapaterMapper {

    // -----------------------------
    // ROOT
    // -----------------------------
    @Mapping(target = "id", expression = "java(courseId)")
    @Mapping(target = "sections", source = "contents")
    @Mapping(target = "modules", expression = "java(flattenModules(dto, courseId))")
    CourseContent toDomain(  MoodleAdapterCourseContentsResponseDto dto, @Context Integer courseId);

    // -----------------------------
    // SECTION
    // -----------------------------
    @Mapping(target = "id", source = "id")
    @Mapping(target = "courseId", expression = "java(courseId)")
    @Mapping(target = "position", source = "section")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "summary", source = "summarytext")
    @Mapping(target = "visible", source = "visible")
    @Mapping(target = "moduleIds", expression = "java(extractModuleIds(dto.getModules()))")
    Section toDomain(MoodleAdapterCourseContentDto dto, @Context Integer courseId);

    // -----------------------------
    // MODULE
    // -----------------------------
    @Mapping(target = "id", source = "id")
    @Mapping(target = "courseId", expression = "java(courseId)")
    @Mapping(target = "sectionId", expression = "java(sectionId)") // se asigna en flatten
    @Mapping(target = "name", source = "name")
    @Mapping(target = "modName", source = "modname")
    @Mapping(target = "url", source = "url")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "visible", source = "visible")
    CourseModule toDomain(MoodleAdapterModuleDto dto, @Context Integer courseId, @Context Integer sectionId);
    // -------------------------
    // FLATTEN MODULES
    // -------------------------
    default List<CourseModule> flattenModules(MoodleAdapterCourseContentsResponseDto dto, @Context Integer courseId) {

        if (dto.getContents() == null) {
            return List.of();
        }

        List<CourseModule> modules = new ArrayList<>();

        for (MoodleAdapterCourseContentDto sectionDto : dto.getContents()) {

            for (MoodleAdapterModuleDto moduleDto : sectionDto.getModules()) {

                CourseModule module = toDomain(moduleDto, courseId, sectionDto.getId());
                modules.add(module);
            }
        }

        return modules;
    }

}
