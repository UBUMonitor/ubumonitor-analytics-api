package es.ubu.lsi.ubumonitoranalytics.features.users.info.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;

public interface SyncUserInfoUseCase {

  UserInfo syncActualUser(UserInfo userInfo);

  UserInfo syncActualUser();
}
