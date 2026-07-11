package es.ubu.lsi.ubumonitoranalytics.features.course.content.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Modules.MODULES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Sections.SECTIONS;

import es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.out.GetCourseContentPersistenceUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseContent;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseModule;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.Section;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCourseContentPersistenceAdapter implements GetCourseContentPersistenceUseCase {

  private final CourseContentPersistenceMapper mapper;
  private final Jooq jooq;

  @Override
  public CourseContent getCourseContent(Integer courseId) {

    // 1. Sections
    Map<Integer, Section> sectionMap =
        jooq.dsl()
            .selectFrom(SECTIONS)
            .where(SECTIONS.COURSE_ID.eq(courseId))
            .and(SECTIONS.ACTIVE.eq(true))
            .orderBy(SECTIONS.POSITION.asc())
            .fetchMap(SECTIONS.ID, mapper::toDomain);

    // 2. Modules agrupadas por section_id
    Map<Integer, List<CourseModule>> modulesBySection =
        jooq.dsl()
            .selectFrom(MODULES)
            .where(MODULES.SECTION_ID.in(sectionMap.keySet()))
            .and(MODULES.ACTIVE.eq(true))
            .orderBy(MODULES.POSITION.asc())
            .fetchGroups(MODULES.SECTION_ID, mapper::toDomain);

    // 3. Asignar modules a cada section
    sectionMap
        .values()
        .forEach(
            section ->
                section.setModules(modulesBySection.getOrDefault(section.getId(), List.of())));

    // 4. Response
    return CourseContent.builder().courseId(courseId).sections(sectionMap.values()).build();
  }
}
