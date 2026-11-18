package es.ubu.lsi.ubumonitoranalytics.features.sites.application.service;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Sites.SITES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.SiteInfoApiPort;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.SiteInfoPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model.Site;
import es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.out.persistence.SiteInfoPersistenceAdapter;
import es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.out.persistence.SitePersistenceAdapterMapper;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.SitesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.net.URI;
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

@SpringJUnitConfig(SyncSiteInfoServiceTransactionTest.TestConfig.class)
class SyncSiteInfoServiceTransactionTest {

  @Autowired private SyncSiteInfoService service;
  @Autowired private SiteInfoApiPort siteInfoApiPort;
  @Autowired private SiteInfoPersistencePort persistencePort;
  @Autowired private SitePersistenceAdapterMapper mapper;
  @Autowired private DSLContext dsl;

  private final List<String> callOrder = new ArrayList<>();

  @BeforeEach
  void createSitesTable() {
    dsl.execute("DROP TRIGGER IF EXISTS fail_site_update");
    dsl.execute("DROP TABLE IF EXISTS sites");
    dsl.execute(
        """
        CREATE TABLE sites (
          id INTEGER PRIMARY KEY,
          user_id BIGINT NOT NULL,
          user_name TEXT,
          host TEXT,
          site_name TEXT,
          version_number TEXT,
          release TEXT,
          version TEXT,
          time_zone TEXT,
          created_at BIGINT NOT NULL DEFAULT 0,
          updated_at BIGINT NOT NULL DEFAULT 0
        )
        """);

    callOrder.clear();
    doAnswer(
            invocation -> {
              assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
              callOrder.add("persist");
              return toRecord((SiteInfo) invocation.getArgument(0));
            })
        .when(mapper)
        .toRecord(any());
  }

  @Test
  void fetchesFromMoodleBeforeStartingPersistenceTransaction() {
    SiteInfo result = siteInfo("Initial site name");
    when(siteInfoApiPort.fetchSiteInfo())
        .thenAnswer(
            invocation -> {
              assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
              callOrder.add("moodle");
              return result;
            });

    assertEquals(result, service.syncSiteInfo());
    assertEquals(List.of("moodle", "persist"), callOrder);
    assertEquals(
        "Initial site name",
        dsl.select(SITES.SITE_NAME).from(SITES).where(SITES.ID.eq(1)).fetchOne(SITES.SITE_NAME));
  }

  @Test
  void failedPersistenceDoesNotLeavePartiallyUpdatedSiteData() {
    persistencePort.save(siteInfo("Existing site name"));
    callOrder.clear();
    dsl.execute(
        """
        CREATE TRIGGER fail_site_update BEFORE UPDATE ON sites
        BEGIN
          UPDATE sites SET site_name = 'partial update' WHERE id = NEW.id;
          SELECT RAISE(ABORT, 'forced persistence failure');
        END
        """);

    when(siteInfoApiPort.fetchSiteInfo())
        .thenAnswer(
            invocation -> {
              assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
              callOrder.add("moodle");
              return siteInfo("Replacement site name");
            });

    assertThrows(RuntimeException.class, service::syncSiteInfo);

    assertEquals(List.of("moodle", "persist"), callOrder);
    assertEquals(
        "Existing site name",
        dsl.select(SITES.SITE_NAME).from(SITES).where(SITES.ID.eq(1)).fetchOne(SITES.SITE_NAME));
  }

  private SitesRecord toRecord(SiteInfo siteInfo) {
    Site site = siteInfo.getSite();
    SitesRecord record = dsl.newRecord(SITES);
    record.setId(site.getId().intValue());
    record.setUserId(site.getUserId());
    record.setUserName(site.getUserName());
    record.setHost(site.getHost().toString());
    record.setSiteName(site.getSiteName());
    record.setVersionNumber(site.getVersionNumber());
    record.setRelease(site.getRelease());
    record.setVersion(site.getVersion());
    record.setCreatedAt(0L);
    record.setUpdatedAt(0L);
    return record;
  }

  private SiteInfo siteInfo(String siteName) {
    Site site = new Site();
    site.setId(1L);
    site.setUserId(13L);
    site.setUserName("teacher");
    site.setHost(URI.create("https://school.moodledemo.net"));
    site.setSiteName(siteName);
    site.setVersionNumber("2026042000");
    site.setRelease("5.2");
    site.setVersion("5.2");
    return new SiteInfo(site);
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
    SitePersistenceAdapterMapper mapper() {
      return org.mockito.Mockito.mock(SitePersistenceAdapterMapper.class);
    }

    @Bean
    SiteInfoApiPort siteInfoApiPort() {
      return org.mockito.Mockito.mock(SiteInfoApiPort.class);
    }

    @Bean
    SiteInfoPersistenceAdapter persistenceAdapter(Jooq jooq, SitePersistenceAdapterMapper mapper) {
      return new SiteInfoPersistenceAdapter(jooq, mapper);
    }

    @Bean
    SyncSiteInfoService syncSiteInfoService(
        SiteInfoApiPort siteInfoApiPort, SiteInfoPersistencePort persistencePort) {
      return new SyncSiteInfoService(siteInfoApiPort, persistencePort);
    }
  }
}
