package es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Sites.SITES;

import es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.out.CurrentUserPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserPersistenceAdapter implements CurrentUserPersistencePort {
  private final Jooq jooq;

  @Override
  public Integer getUserId(String userName) {
    return jooq.dsl()
        .select(SITES.USER_ID)
        .from(SITES)
        .where(SITES.USER_NAME.eq(userName))
        .fetchOneInto(Integer.class);
  }
}
