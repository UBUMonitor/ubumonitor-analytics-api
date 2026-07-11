package es.ubu.lsi.ubumonitoranalytics.features.course.content.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Courses.COURSES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Modules.MODULES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Sections.SECTIONS;

import es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.out.CourseContentPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseContent;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseModule;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.Section;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.ModulesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.SectionsRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SyncCourseContentPersistenceAdapter implements CourseContentPersistencePort {

  private final Jooq jooq;
  private final CourseContentPersistenceMapper mapper;

  @Override
  public void save(CourseContent content) {

    Integer courseId = content.getCourseId();
    LocalDateTime now = LocalDateTime.now();
    upsertCourse(courseId);
    deactivateExisting(courseId);

    upsertSections(content, now);
    upsertModules(content, now);
  }

  private void upsertSections(CourseContent content, LocalDateTime now) {
    if (content.getSections() == null || content.getSections().isEmpty()) {
      return;
    }

    List<SectionsRecord> records = new ArrayList<>();

    for (Section section : content.getSections()) {

      SectionsRecord r = jooq.dsl().newRecord(SECTIONS);

      mapper.toRecord(section, r);

      r.setCourseId(content.getCourseId());
      r.setActive(true);
      r.setUpdatedAt(now);

      records.add(r);
    }

    jooq.dsl().batchMerge(records).execute();
  }

  private void upsertModules(CourseContent content, LocalDateTime now) {

    List<ModulesRecord> records = new ArrayList<>();

    for (Section section : content.getSections()) {

      if (section.getModules() == null) {
        continue;
      }

      int position = 0;

      for (CourseModule module : section.getModules()) {

        ModulesRecord mr = jooq.dsl().newRecord(MODULES);

        mapper.toRecord(module, mr);

        mr.setSectionId(section.getId());
        mr.setActive(true);
        mr.setUpdatedAt(now);
        mr.setPosition(position++);

        records.add(mr);
      }
    }

    jooq.dsl().batchMerge(records).execute();
  }

  private void upsertCourse(Integer courseId) {
    jooq.dsl()
        .insertInto(COURSES)
        .set(COURSES.ID, courseId)
        .onConflict(COURSES.ID)
        .doNothing()
        .execute();
  }

  private void deactivateExisting(Integer courseId) {

    jooq.dsl()
        .update(MODULES)
        .set(MODULES.ACTIVE, false)
        .set(MODULES.UPDATED_AT, DSL.currentLocalDateTime())
        .where(MODULES.ACTIVE.eq(true))
        .andExists(
            jooq.dsl()
                .selectOne()
                .from(SECTIONS)
                .where(SECTIONS.ID.eq(MODULES.SECTION_ID))
                .and(SECTIONS.COURSE_ID.eq(courseId)))
        .execute();

    jooq.dsl()
        .update(SECTIONS)
        .set(SECTIONS.ACTIVE, false)
        .set(SECTIONS.UPDATED_AT, DSL.currentLocalDateTime())
        .where(SECTIONS.COURSE_ID.eq(courseId))
        .and(SECTIONS.ACTIVE.eq(true))
        .execute();
  }
}
