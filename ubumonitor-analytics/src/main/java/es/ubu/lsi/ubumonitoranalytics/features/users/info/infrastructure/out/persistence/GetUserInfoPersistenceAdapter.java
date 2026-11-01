package es.ubu.lsi.ubumonitoranalytics.features.users.info.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.ActualUser.ACTUAL_USER;

import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.out.GetUserInfoPersistenceAdapterPort;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.infrastructure.out.persistence.mapper.UserInfoPersistenceMapper;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetUserInfoPersistenceAdapter implements GetUserInfoPersistenceAdapterPort {

  private final UserInfoPersistenceMapper mapper;
  private final Jooq jooq;

  @Override
  public UserInfo getActualUser(String userName) {
    return jooq.dsl()
        .selectFrom(ACTUAL_USER)
        .where(ACTUAL_USER.USER_NAME.eq(userName))
        .fetchOne(mapper::toDomain);
  }
}
