package es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;

public interface SyncUserInfoUseCase {

  UserInfo syncActualUser(String userName);

  UserInfo syncActualUser();
}
