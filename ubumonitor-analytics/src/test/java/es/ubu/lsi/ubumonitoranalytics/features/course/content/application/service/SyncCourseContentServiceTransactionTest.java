package es.ubu.lsi.ubumonitoranalytics.features.course.content.application.service;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Courses.COURSES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Modules.MODULES;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Sections.SECTIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.in.SyncCourseContentUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.out.CourseContentPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.out.FetchCourseContentPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseContent;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseModule;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.Section;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.infrastructure.out.persistence.CourseContentPersistenceMapper;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.infrastructure.out.persistence.SyncCourseContentPersistenceAdapter;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.ModulesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.SectionsRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.util.ArrayList;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringJUnitConfig(SyncCourseContentServiceTransactionTest.TestConfig.class)
class SyncCourseContentServiceTransactionTest {

  @Autowired private SyncCourseContentUseCase service;
  @Autowired private FetchCourseContentPort moodlePort;
  @Autowired private CourseContentPersistenceMapper mapper;
  @Autowired private DSLContext dsl;

  private final List<String> callOrder = new ArrayList<>();

  @BeforeEach
  void createCourseContentTables() {
    dsl.execute("DROP TRIGGER IF EXISTS fail_module_insert");
    dsl.execute("DROP TABLE IF EXISTS modules");
    dsl.execute("DROP TABLE IF EXISTS sections");
    dsl.execute("DROP TABLE IF EXISTS courses");
    dsl.execute("CREATE TABLE courses (id BIGINT PRIMARY KEY)");
    dsl.execute(
        """
        CREATE TABLE sections (
          id BIGINT PRIMARY KEY,
          course_id BIGINT NOT NULL,
          name TEXT,
          summary TEXT,
          position INTEGER,
          visible BOOLEAN NOT NULL DEFAULT TRUE,
          active BOOLEAN NOT NULL DEFAULT TRUE,
          created_at BIGINT NOT NULL DEFAULT 0,
          updated_at BIGINT NOT NULL DEFAULT 0
        )
        """);
    dsl.execute(
        """
        CREATE TABLE modules (
          id BIGINT PRIMARY KEY,
          section_id BIGINT NOT NULL,
          name TEXT,
          mod_name TEXT,
          instance BIGINT,
          url TEXT,
          description TEXT,
          visible BOOLEAN NOT NULL DEFAULT TRUE,
          position INTEGER,
          active BOOLEAN NOT NULL DEFAULT TRUE,
          created_at BIGINT NOT NULL DEFAULT 0,
          updated_at BIGINT NOT NULL DEFAULT 0
        )
        """);

    callOrder.clear();
    doAnswer(
            invocation -> {
              assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
              callOrder.add("section");
              Section section = invocation.getArgument(0);
              SectionsRecord record = invocation.getArgument(1);
              record.setId(section.getId());
              record.setName(section.getName());
              record.setSummary(section.getSummary());
              record.setPosition(section.getPosition());
              record.setVisible(section.getVisible());
              return null;
            })
        .when(mapper)
        .toRecord(any(Section.class), any(SectionsRecord.class));
    doAnswer(
            invocation -> {
              assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
              callOrder.add("module");
              CourseModule module = invocation.getArgument(0);
              ModulesRecord record = invocation.getArgument(1);
              record.setId(module.getId());
              record.setName(module.getName());
              record.setModName(module.getModName());
              record.setInstance(module.getInstance());
              record.setDescription(module.getDescription());
              record.setVisible(module.getVisible());
              return null;
            })
        .when(mapper)
        .toRecord(any(CourseModule.class), any(ModulesRecord.class));
  }

  @Test
  void fetchesFromMoodleBeforeStartingPersistenceTransaction() {
    CourseContent content = courseContent(7L, 2L, 22L, "Fetched section", "Fetched module");
    when(moodlePort.fetchCourseContent(7L))
        .thenAnswer(
            invocation -> {
              assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
              callOrder.add("moodle");
              return content;
            });

    assertEquals(content, service.syncCourseContent(7L));

    assertEquals(List.of("moodle", "section", "module"), callOrder);
    assertEquals(
        "Fetched section", dsl.select(SECTIONS.NAME).from(SECTIONS).fetchOne(SECTIONS.NAME));
    assertEquals("Fetched module", dsl.select(MODULES.NAME).from(MODULES).fetchOne(MODULES.NAME));
  }

  @Test
  void failedPersistenceRollsBackAllCourseContentChanges() {
    dsl.insertInto(SECTIONS)
        .set(SECTIONS.ID, 1L)
        .set(SECTIONS.COURSE_ID, 7L)
        .set(SECTIONS.NAME, "Existing section")
        .set(SECTIONS.ACTIVE, true)
        .execute();
    dsl.insertInto(MODULES)
        .set(MODULES.ID, 11L)
        .set(MODULES.SECTION_ID, 1L)
        .set(MODULES.NAME, "Existing module")
        .set(MODULES.ACTIVE, true)
        .execute();
    dsl.execute(
        """
        CREATE TRIGGER fail_module_insert BEFORE INSERT ON modules
        WHEN NEW.id = 22
        BEGIN
          SELECT RAISE(ABORT, 'forced persistence failure');
        END
        """);
    when(moodlePort.fetchCourseContent(7L))
        .thenAnswer(
            invocation -> {
              assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
              callOrder.add("moodle");
              return courseContent(7L, 2L, 22L, "Replacement section", "Replacement module");
            });

    assertThrows(RuntimeException.class, () -> service.syncCourseContent(7L));

    assertEquals(List.of("moodle", "section", "module"), callOrder);
    assertEquals(
        0, dsl.selectCount().from(COURSES).where(COURSES.ID.eq(7L)).fetchOne(0, int.class));
    assertEquals(
        "Existing section",
        dsl.select(SECTIONS.NAME).from(SECTIONS).where(SECTIONS.ID.eq(1L)).fetchOne(SECTIONS.NAME));
    assertTrue(
        dsl.select(SECTIONS.ACTIVE)
            .from(SECTIONS)
            .where(SECTIONS.ID.eq(1L))
            .fetchOne(SECTIONS.ACTIVE));
    assertEquals(
        "Existing module",
        dsl.select(MODULES.NAME).from(MODULES).where(MODULES.ID.eq(11L)).fetchOne(MODULES.NAME));
    assertTrue(
        dsl.select(MODULES.ACTIVE)
            .from(MODULES)
            .where(MODULES.ID.eq(11L))
            .fetchOne(MODULES.ACTIVE));
    assertEquals(
        0, dsl.selectCount().from(SECTIONS).where(SECTIONS.ID.eq(2L)).fetchOne(0, int.class));
  }

  private CourseContent courseContent(
      Long courseId, Long sectionId, Long moduleId, String sectionName, String moduleName) {
    CourseModule module = new CourseModule();
    module.setId(moduleId);
    module.setName(moduleName);
    module.setModName("resource");
    module.setVisible(true);

    Section section = new Section();
    section.setId(sectionId);
    section.setName(sectionName);
    section.setPosition(0);
    section.setVisible(true);
    section.setModules(List.of(module));

    return CourseContent.builder().courseId(courseId).sections(List.of(section)).build();
  }

  @Configuration
  @EnableTransactionManagement
  static class TestConfig {

    @Bean
    SingleConnectionDataSource dataSource() {
      return new SingleConnectionDataSource("jdbc:sqlite::memory:", true);
    }

    @Bean
    PlatformTransactionManager transactionManager(SingleConnectionDataSource dataSource) {
      return new JdbcTransactionManager(dataSource);
    }

    @Bean
    DSLContext dsl(SingleConnectionDataSource dataSource) {
      return DSL.using(new TransactionAwareDataSourceProxy(dataSource), SQLDialect.SQLITE);
    }

    @Bean
    Jooq jooq(DSLContext dsl) {
      Jooq jooq = org.mockito.Mockito.mock(Jooq.class);
      when(jooq.dsl()).thenReturn(dsl);
      return jooq;
    }

    @Bean
    CourseContentPersistenceMapper mapper() {
      return org.mockito.Mockito.mock(CourseContentPersistenceMapper.class);
    }

    @Bean
    FetchCourseContentPort moodlePort() {
      return org.mockito.Mockito.mock(FetchCourseContentPort.class);
    }

    @Bean
    CourseContentPersistencePort persistenceAdapter(
        Jooq jooq, CourseContentPersistenceMapper mapper) {
      return new SyncCourseContentPersistenceAdapter(jooq, mapper);
    }

    @Bean
    SyncCourseContentUseCase service(
        CourseContentPersistencePort persistencePort, FetchCourseContentPort moodlePort) {
      return new SyncCourseContentService(persistencePort, moodlePort);
    }
  }
}
