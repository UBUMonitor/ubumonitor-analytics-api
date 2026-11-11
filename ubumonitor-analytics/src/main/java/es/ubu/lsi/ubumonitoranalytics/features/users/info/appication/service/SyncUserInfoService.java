package es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.service;

import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.in.SearchUserByUserNameUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.in.SyncUserInfoUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.out.GetUserInfoPersistenceAdapterPort;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.out.SearchUsersByFieldPort;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.out.ServerTimeZonePort;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.out.SyncUserInfoPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.CurrentSessionPort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.NotFoundException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.LoginMode;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncUserInfoService implements SyncUserInfoUseCase, SearchUserByUserNameUseCase {

  private final CurrentSessionPort currentSessionContext;
  private final SearchUsersByFieldPort searchUsersByFieldPort;
  private final ServerTimeZonePort serverTimeZonePort;
  private final SyncUserInfoPersistencePort syncUserInfoPersistencePort;
  private final GetUserInfoPersistenceAdapterPort getUserInfoPersistenceAdapterPort;

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
    SessionData sessionData = currentSessionContext.getSessionData();
    String userName = sessionData.getUsername();
    if (sessionData.getLoginMode() == LoginMode.OFFLINE) {
      UserInfo userInfo = getUserInfoPersistenceAdapterPort.getActualUser(userName);
      if (userInfo == null) {
        throw new NotFoundException("User not found " + userName);
      }
      return userInfo;
    }
    return syncActualUser(searchUserByUserName(userName));
  }
}
