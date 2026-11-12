package es.ubu.lsi.ubumonitoranalytics.features.users.info.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;

public interface SyncUserInfoPersistencePort {
  void upsertActualUser(UserInfo userInfo);
}
