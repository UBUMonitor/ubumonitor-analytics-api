package es.ubu.lsi.ubumonitoranalytics.features.users.info.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.features.users.info.application.port.out.GetUserInfoPersistenceAdapterPort;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.application.port.out.SearchUsersByFieldPort;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.application.port.out.ServerTimeZonePort;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.application.port.out.SyncUserInfoPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.CurrentSessionPort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.NotFoundException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.LoginMode;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class SyncUserInfoServiceTest {

  @Test
  void searchUserByUserNameOnlyLooksUpTheUser() {
    SearchUsersByFieldPort searchPort = mock(SearchUsersByFieldPort.class);
    ServerTimeZonePort timeZonePort = mock(ServerTimeZonePort.class);
    SyncUserInfoPersistencePort persistencePort = mock(SyncUserInfoPersistencePort.class);
    SyncUserInfoService service = service(searchPort, timeZonePort, persistencePort);
    UserInfo userInfo = serverTimeZoneUserInfo();
    when(searchPort.getUserByUserName("teacher")).thenReturn(userInfo);

    UserInfo result = service.searchUserByUserName("teacher");

    assertSame(userInfo, result);
    assertNull(result.getTimeZone());
    verify(searchPort).getUserByUserName("teacher");
    verifyNoInteractions(timeZonePort, persistencePort);
  }

  @Test
  void syncActualUserCompletesSearchResultTimezoneAndPersistence() {
    SearchUsersByFieldPort searchPort = mock(SearchUsersByFieldPort.class);
    ServerTimeZonePort timeZonePort = mock(ServerTimeZonePort.class);
    SyncUserInfoPersistencePort persistencePort = mock(SyncUserInfoPersistencePort.class);
    SyncUserInfoService service = service(searchPort, timeZonePort, persistencePort);
    UserInfo userInfo = serverTimeZoneUserInfo();
    ZoneId serverTimeZone = ZoneId.of("Europe/Madrid");
    when(timeZonePort.getServerTimezone()).thenReturn(serverTimeZone);

    UserInfo result = service.syncActualUser(userInfo);

    assertSame(userInfo, result);
    assertEquals(serverTimeZone, result.getTimeZone());
    InOrder inOrder = inOrder(timeZonePort, persistencePort);
    inOrder.verify(timeZonePort).getServerTimezone();
    inOrder.verify(persistencePort).upsertActualUser(userInfo);
    verifyNoInteractions(searchPort);
  }

  @Test
  void syncActualUserUsesSessionUsernameThenCompletesLookup() {
    SearchUsersByFieldPort searchPort = mock(SearchUsersByFieldPort.class);
    ServerTimeZonePort timeZonePort = mock(ServerTimeZonePort.class);
    SyncUserInfoPersistencePort persistencePort = mock(SyncUserInfoPersistencePort.class);
    CurrentSessionPort currentSessionContext = mock(CurrentSessionPort.class);
    GetUserInfoPersistenceAdapterPort getUserInfoPersistenceAdapterPort =
        mock(GetUserInfoPersistenceAdapterPort.class);
    SyncUserInfoService service =
        new SyncUserInfoService(
            currentSessionContext,
            searchPort,
            timeZonePort,
            persistencePort,
            getUserInfoPersistenceAdapterPort);
    UserInfo userInfo = serverTimeZoneUserInfo();
    when(currentSessionContext.getSessionData())
        .thenReturn(
            SessionData.builder().username("teacher").loginMode(LoginMode.CREDENTIALS).build());
    when(searchPort.getUserByUserName("teacher")).thenReturn(userInfo);
    when(timeZonePort.getServerTimezone()).thenReturn(ZoneId.of("Europe/Madrid"));

    UserInfo result = service.syncActualUser();

    assertSame(userInfo, result);
    InOrder inOrder = inOrder(searchPort, timeZonePort, persistencePort);
    inOrder.verify(searchPort).getUserByUserName("teacher");
    inOrder.verify(timeZonePort).getServerTimezone();
    inOrder.verify(persistencePort).upsertActualUser(userInfo);
    verifyNoInteractions(getUserInfoPersistenceAdapterPort);
  }

  @Test
  void syncActualUserReturnsLocallyPersistedUserForOfflineSession() {
    SearchUsersByFieldPort searchPort = mock(SearchUsersByFieldPort.class);
    ServerTimeZonePort timeZonePort = mock(ServerTimeZonePort.class);
    SyncUserInfoPersistencePort persistencePort = mock(SyncUserInfoPersistencePort.class);
    GetUserInfoPersistenceAdapterPort getUserInfoPersistenceAdapterPort =
        mock(GetUserInfoPersistenceAdapterPort.class);
    CurrentSessionPort currentSessionContext = mock(CurrentSessionPort.class);
    SyncUserInfoService service =
        new SyncUserInfoService(
            currentSessionContext,
            searchPort,
            timeZonePort,
            persistencePort,
            getUserInfoPersistenceAdapterPort);
    UserInfo userInfo = new UserInfo();
    when(currentSessionContext.getSessionData())
        .thenReturn(SessionData.builder().username("teacher").loginMode(LoginMode.OFFLINE).build());
    when(getUserInfoPersistenceAdapterPort.getActualUser("teacher")).thenReturn(userInfo);

    UserInfo result = service.syncActualUser();

    assertSame(userInfo, result);
    verify(getUserInfoPersistenceAdapterPort).getActualUser("teacher");
    verifyNoInteractions(searchPort, timeZonePort, persistencePort);
  }

  @Test
  void syncActualUserReportsMissingLocalUserForOfflineSession() {
    SearchUsersByFieldPort searchPort = mock(SearchUsersByFieldPort.class);
    ServerTimeZonePort timeZonePort = mock(ServerTimeZonePort.class);
    SyncUserInfoPersistencePort persistencePort = mock(SyncUserInfoPersistencePort.class);
    GetUserInfoPersistenceAdapterPort getUserInfoPersistenceAdapterPort =
        mock(GetUserInfoPersistenceAdapterPort.class);
    CurrentSessionPort currentSessionContext = mock(CurrentSessionPort.class);
    SyncUserInfoService service =
        new SyncUserInfoService(
            currentSessionContext,
            searchPort,
            timeZonePort,
            persistencePort,
            getUserInfoPersistenceAdapterPort);
    when(currentSessionContext.getSessionData())
        .thenReturn(SessionData.builder().username("teacher").loginMode(LoginMode.OFFLINE).build());

    assertThrows(NotFoundException.class, service::syncActualUser);

    verify(getUserInfoPersistenceAdapterPort).getActualUser("teacher");
    verifyNoInteractions(searchPort, timeZonePort, persistencePort);
  }

  private static SyncUserInfoService service(
      SearchUsersByFieldPort searchPort,
      ServerTimeZonePort timeZonePort,
      SyncUserInfoPersistencePort persistencePort) {
    return new SyncUserInfoService(
        mock(CurrentSessionPort.class),
        searchPort,
        timeZonePort,
        persistencePort,
        mock(GetUserInfoPersistenceAdapterPort.class));
  }

  private static UserInfo serverTimeZoneUserInfo() {
    UserInfo userInfo = new UserInfo();
    userInfo.setServerTimeZone(true);
    return userInfo;
  }
}
