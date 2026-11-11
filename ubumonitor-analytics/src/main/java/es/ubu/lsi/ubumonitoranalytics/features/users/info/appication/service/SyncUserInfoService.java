package es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.service;

import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.in.SearchUserByUserNameUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.in.SyncUserInfoUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.out.SearchUsersByFieldPort;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.out.ServerTimeZonePort;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.out.SyncUserInfoPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.CurrentSessionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncUserInfoService implements SyncUserInfoUseCase, SearchUserByUserNameUseCase {

  private final CurrentSessionPort currentSessionContext;
  private final SearchUsersByFieldPort searchUsersByFieldPort;
  private final ServerTimeZonePort serverTimeZonePort;
  private final SyncUserInfoPersistencePort syncUserInfoPersistencePort;

  @Override
  public UserInfo searchUserByUserName(String userName) {
    return searchUsersByFieldPort.getUserByUserName(userName);
  }

  @Override
  public UserInfo syncActualUser(UserInfo userInfo) {
    if (userInfo.isServerTimeZone()) {
      userInfo.setTimeZone(serverTimeZonePort.getServerTimezone());
    }

    syncUserInfoPersistencePort.upsertActualUser(userInfo);
    return userInfo;
  }

  @Override
  public UserInfo syncActualUser() {

    String userName = currentSessionContext.getSessionData().getUsername();
    return syncActualUser(searchUserByUserName(userName));
  }
}
