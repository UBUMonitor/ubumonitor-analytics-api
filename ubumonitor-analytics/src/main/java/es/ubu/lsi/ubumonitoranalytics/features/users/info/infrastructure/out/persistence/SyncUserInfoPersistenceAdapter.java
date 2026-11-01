package es.ubu.lsi.ubumonitoranalytics.features.users.info.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.out.SyncUserInfoPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.infrastructure.out.persistence.mapper.UserInfoPersistenceMapper;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.ActualUserRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SyncUserInfoPersistenceAdapter implements SyncUserInfoPersistencePort {

  private final Jooq jooq;
  private final UserInfoPersistenceMapper mapper;

  @Override
  public void upsertActualUser(UserInfo userInfo) {
    ActualUserRecord actualUserRecord = mapper.toRecord(userInfo);
    jooq.dsl().batchMerge(actualUserRecord).execute();
  }
}
