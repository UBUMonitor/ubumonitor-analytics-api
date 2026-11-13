package es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.application.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.application.port.in.CourseActivityCompletionUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.application.port.out.CourseActivityCompletionPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.application.port.out.CourseUsersPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.application.port.out.FetchActivityCompletionPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.application.port.out.GetCourseActivityCompletionPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.domain.model.ActivityCompletionResult;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringJUnitConfig(CourseActivityCompletionServiceTransactionTest.TestConfig.class)
class CourseActivityCompletionServiceTransactionTest {

  @Autowired private CourseActivityCompletionUseCase service;
  @Autowired private CourseActivityCompletionPersistencePort persistencePort;
  @Autowired private FetchActivityCompletionPort fetchActivityCompletionPort;
  @Autowired private GetCourseActivityCompletionPersistencePort getPersistencePort;
  @Autowired private CourseUsersPort courseUsersPort;

  @Test
  void bothReadOperationsRunInReadOnlyTransactions() {
    ActivityCompletionResult result =
        ActivityCompletionResult.builder().courseId(7L).activityCompletions(List.of()).build();
    when(getPersistencePort.getCourseActivitiesCompletions(7L))
        .thenAnswer(
            invocation -> {
              assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
              assertTrue(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
              return result;
            });
    when(getPersistencePort.getCourseActivitiesCompletions(7L, List.of(11L), List.of(19L)))
        .thenAnswer(
            invocation -> {
              assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
              assertTrue(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
              return result;
            });

    assertSame(result, service.getActivityCompletion(7L));
    assertSame(result, service.getActivityCompletion(7L, List.of(11L), List.of(19L)));
  }

  @Test
  void syncRemainsOutsideReadOnlyTransaction() {
    ActivityCompletionResult result =
        ActivityCompletionResult.builder().courseId(7L).activityCompletions(List.of()).build();
    when(courseUsersPort.getCourseUsersIds(7L))
        .thenAnswer(
            invocation -> {
              assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
              return List.of(19L);
            });
    when(fetchActivityCompletionPort.fetchCourseActivityCompletion(7L, List.of(19L)))
        .thenAnswer(
            invocation -> {
              assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
              return result;
            });
    org.mockito.Mockito.doAnswer(
            invocation -> {
              assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
              return null;
            })
        .when(persistencePort)
        .save(result);

    assertSame(result, service.syncActivityCompletion(7L));
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
    CourseActivityCompletionPersistencePort persistencePort() {
      return org.mockito.Mockito.mock(CourseActivityCompletionPersistencePort.class);
    }

    @Bean
    FetchActivityCompletionPort fetchActivityCompletionPort() {
      return org.mockito.Mockito.mock(FetchActivityCompletionPort.class);
    }

    @Bean
    GetCourseActivityCompletionPersistencePort getPersistencePort() {
      return org.mockito.Mockito.mock(GetCourseActivityCompletionPersistencePort.class);
    }

    @Bean
    CourseUsersPort courseUsersPort() {
      return org.mockito.Mockito.mock(CourseUsersPort.class);
    }

    @Bean
    CourseActivityCompletionUseCase service(
        CourseActivityCompletionPersistencePort persistencePort,
        FetchActivityCompletionPort fetchActivityCompletionPort,
        GetCourseActivityCompletionPersistencePort getPersistencePort,
        CourseUsersPort courseUsersPort) {
      return new CourseActivityCompletionService(
          persistencePort, fetchActivityCompletionPort, getPersistencePort, courseUsersPort);
    }
  }
}
