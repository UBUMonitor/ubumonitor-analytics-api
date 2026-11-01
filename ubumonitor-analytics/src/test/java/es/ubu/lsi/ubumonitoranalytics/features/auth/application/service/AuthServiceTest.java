package es.ubu.lsi.ubumonitoranalytics.features.auth.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import es.ubu.lsi.moodle.exception.MoodleUnauthorizedException;
import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out.MoodleApiPort;
import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out.SessionPort;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.SessionToken;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.SiteInfoApiPort;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.SiteInfoPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model.Site;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.in.SyncUserInfoUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.out.GetUserInfoPersistenceAdapterPort;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.UnauthorizedException;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.database.TenantDatabasePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.DatabaseCreationException;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.concurrent.AsyncOperations;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class AuthServiceTest {

  @Test
  void loginBySSOResolvesTheMoodleUserAndSynchronizesUserInfo() {
    SessionPort sessionPort = mock(SessionPort.class);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    TenantDatabasePort tenantDatabasePort = mock(TenantDatabasePort.class);
    SyncUserInfoUseCase syncUserInfoUseCase = mock(SyncUserInfoUseCase.class);
    SiteInfoApiPort siteInfoApiPort = mock(SiteInfoApiPort.class);
    SiteInfoPersistencePort siteInfoPersistencePort = mock(SiteInfoPersistencePort.class);
    AuthInput authInput =
        new AuthInput(
            null,
            null,
            URI.create("https://moodle.test"),
            "database-password",
            "moodle-token",
            null);
    SessionToken sessionToken = mock(SessionToken.class);
    RestClient restClient = mock(RestClient.class);
    UserInfo userInfo = mock(UserInfo.class);
    Site site = new Site();
    site.setUserName("teacher");
    SiteInfo siteInfo = SiteInfo.builder().site(site).build();
    AsyncOperations asyncOperations = new AsyncOperations(Runnable::run);

    when(sessionPort.generateSession(authInput, null, null)).thenReturn(sessionToken);
    when(siteInfoApiPort.fetchSiteInfo()).thenReturn(siteInfo);
    when(moodleApiPort.getRestClientFromCookies(authInput)).thenReturn(restClient);
    when(sessionPort.updateSession(authInput, sessionToken, null, restClient))
        .thenReturn(sessionToken);
    when(syncUserInfoUseCase.syncActualUser("teacher")).thenReturn(userInfo);
    when(sessionPort.updateSession(authInput, sessionToken, userInfo, restClient))
        .thenReturn(sessionToken);

    AuthService authService =
        new AuthService(
            syncUserInfoUseCase,
            mock(GetUserInfoPersistenceAdapterPort.class),
            siteInfoApiPort,
            siteInfoPersistencePort,
            sessionPort,
            moodleApiPort,
            tenantDatabasePort,
            asyncOperations);

    authService.loginBySSO(authInput);

    verify(tenantDatabasePort).initializeTenant(any());
    verify(siteInfoPersistencePort).save(siteInfo);
    verify(syncUserInfoUseCase).syncActualUser("teacher");
    verify(sessionPort).updateSession(authInput, sessionToken, userInfo, restClient);
  }

  @Test
  void invalidSSOUserInvalidatesTheProvisionalSession() {
    SessionPort sessionPort = mock(SessionPort.class);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    SiteInfoApiPort siteInfoApiPort = mock(SiteInfoApiPort.class);
    SessionToken sessionToken = mock(SessionToken.class);

    when(sessionPort.generateSession(any(), isNull(), isNull())).thenReturn(sessionToken);
    when(siteInfoApiPort.fetchSiteInfo()).thenReturn(new SiteInfo());

    AuthService authService =
        new AuthService(
            mock(SyncUserInfoUseCase.class),
            mock(GetUserInfoPersistenceAdapterPort.class),
            siteInfoApiPort,
            mock(SiteInfoPersistencePort.class),
            sessionPort,
            moodleApiPort,
            mock(TenantDatabasePort.class),
            new AsyncOperations(Runnable::run));

    assertThrows(UnauthorizedException.class, () -> authService.loginBySSO(new AuthInput()));

    verify(sessionPort).invalidateSession(sessionToken);
    verifyNoInteractions(moodleApiPort);
  }

  @Test
  void doesNotInitializeTenantDatabaseWhenMoodleRejectsCredentials() {
    SessionPort sessionPort = mock(SessionPort.class);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    TenantDatabasePort tenantDatabasePort = mock(TenantDatabasePort.class);
    AsyncOperations asyncOperations = new AsyncOperations(Runnable::run);

    when(moodleApiPort.login(any())).thenReturn(null);
    when(moodleApiPort.loginWeb("teacher", "wrong-password", URI.create("https://moodle.test")))
        .thenReturn(mock(RestClient.class));

    AuthService authService =
        new AuthService(
            mock(SyncUserInfoUseCase.class),
            mock(GetUserInfoPersistenceAdapterPort.class),
            mock(SiteInfoApiPort.class),
            mock(SiteInfoPersistencePort.class),
            sessionPort,
            moodleApiPort,
            tenantDatabasePort,
            asyncOperations);
    AuthInput authInput =
        new AuthInput(
            "teacher",
            "wrong-password",
            URI.create("https://moodle.test"),
            "db-password",
            null,
            null);

    assertThrows(UnauthorizedException.class, () -> authService.loginByCredentials(authInput));
    verify(moodleApiPort).login(authInput);
    verifyNoInteractions(tenantDatabasePort, sessionPort);
  }

  @Test
  void mapsMoodleUnauthorizedExceptionToApplicationUnauthorizedException() {
    SessionPort sessionPort = mock(SessionPort.class);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    TenantDatabasePort tenantDatabasePort = mock(TenantDatabasePort.class);
    AsyncOperations asyncOperations = new AsyncOperations(Runnable::run);
    AuthInput authInput =
        new AuthInput(
            "teacher",
            "wrong-password",
            URI.create("https://moodle.test"),
            "db-password",
            null,
            null);

    when(moodleApiPort.login(authInput))
        .thenThrow(new MoodleUnauthorizedException("Invalid login"));

    AuthService authService =
        new AuthService(
            mock(SyncUserInfoUseCase.class),
            mock(GetUserInfoPersistenceAdapterPort.class),
            mock(SiteInfoApiPort.class),
            mock(SiteInfoPersistencePort.class),
            sessionPort,
            moodleApiPort,
            tenantDatabasePort,
            asyncOperations);

    assertThrows(UnauthorizedException.class, () -> authService.loginByCredentials(authInput));
    verify(moodleApiPort).login(authInput);
    verifyNoInteractions(tenantDatabasePort, sessionPort);
  }

  @Test
  void doesNotCreateSessionWhenTenantDatabasePasswordIsRejected() {
    SessionPort sessionPort = mock(SessionPort.class);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    TenantDatabasePort tenantDatabasePort = mock(TenantDatabasePort.class);
    AsyncOperations asyncOperations = new AsyncOperations(Runnable::run);
    AuthInput authInput =
        new AuthInput(
            "teacher",
            "moodle-password",
            URI.create("https://moodle.test"),
            "wrong-database-password",
            null,
            null);

    when(moodleApiPort.login(authInput)).thenReturn("moodle-token");
    when(moodleApiPort.loginWeb("teacher", "moodle-password", authInput.getHost()))
        .thenReturn(mock(RestClient.class));
    doThrow(new DatabaseCreationException("Invalid database password", new IllegalStateException()))
        .when(tenantDatabasePort)
        .initializeTenant(any());

    AuthService authService =
        new AuthService(
            mock(SyncUserInfoUseCase.class),
            mock(GetUserInfoPersistenceAdapterPort.class),
            mock(SiteInfoApiPort.class),
            mock(SiteInfoPersistencePort.class),
            sessionPort,
            moodleApiPort,
            tenantDatabasePort,
            asyncOperations);

    assertThrows(DatabaseCreationException.class, () -> authService.loginByCredentials(authInput));
    verify(tenantDatabasePort).initializeTenant(any());
    verifyNoInteractions(sessionPort);
  }

  @Test
  void synchronizesUserInfoAfterCreatingTenantDatabase() {
    SessionPort sessionPort = mock(SessionPort.class);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    TenantDatabasePort tenantDatabasePort = mock(TenantDatabasePort.class);
    SyncUserInfoUseCase syncUserInfoUseCase = mock(SyncUserInfoUseCase.class);
    SiteInfoApiPort siteInfoApiPort = mock(SiteInfoApiPort.class);
    UserInfo userInfo = mock(UserInfo.class);
    SessionToken sessionToken = mock(SessionToken.class);
    RestClient restClient = mock(RestClient.class);
    AsyncOperations asyncOperations = new AsyncOperations(Runnable::run);
    AuthInput authInput =
        new AuthInput(
            "teacher",
            "moodle-password",
            URI.create("https://moodle.test"),
            "database-password",
            null,
            null);

    when(moodleApiPort.login(authInput)).thenReturn("moodle-token");
    when(moodleApiPort.loginWeb("teacher", "moodle-password", authInput.getHost()))
        .thenReturn(restClient);
    when(tenantDatabasePort.initializeTenant(any())).thenReturn(true);
    when(siteInfoApiPort.fetchSiteInfo()).thenReturn(mock(SiteInfo.class));
    when(syncUserInfoUseCase.syncActualUser("teacher")).thenReturn(userInfo);
    when(sessionPort.generateSession(eq(authInput), isNull(), eq(restClient)))
        .thenReturn(sessionToken);

    AuthService authService =
        new AuthService(
            syncUserInfoUseCase,
            mock(GetUserInfoPersistenceAdapterPort.class),
            siteInfoApiPort,
            mock(SiteInfoPersistencePort.class),
            sessionPort,
            moodleApiPort,
            tenantDatabasePort,
            asyncOperations);

    authService.loginByCredentials(authInput);

    verify(tenantDatabasePort).initializeTenant(any());
    verify(sessionPort).generateSession(eq(authInput), isNull(), eq(restClient));
    verify(sessionPort).updateSession(authInput, sessionToken, userInfo, restClient);
    verify(syncUserInfoUseCase).syncActualUser("teacher");
  }

  @Test
  void synchronizesUserInfoWhenTenantDatabaseAlreadyExists() {
    SessionPort sessionPort = mock(SessionPort.class);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    TenantDatabasePort tenantDatabasePort = mock(TenantDatabasePort.class);
    SyncUserInfoUseCase syncUserInfoUseCase = mock(SyncUserInfoUseCase.class);
    SiteInfoApiPort siteInfoApiPort = mock(SiteInfoApiPort.class);
    UserInfo userInfo = mock(UserInfo.class);
    SessionToken sessionToken = mock(SessionToken.class);
    RestClient restClient = mock(RestClient.class);
    AsyncOperations asyncOperations = new AsyncOperations(Runnable::run);
    AuthInput authInput =
        new AuthInput(
            "teacher",
            "moodle-password",
            URI.create("https://moodle.test"),
            "database-password",
            null,
            null);

    when(moodleApiPort.login(authInput)).thenReturn("moodle-token");
    when(moodleApiPort.loginWeb("teacher", "moodle-password", authInput.getHost()))
        .thenReturn(restClient);
    when(tenantDatabasePort.initializeTenant(any())).thenReturn(false);
    when(siteInfoApiPort.fetchSiteInfo()).thenReturn(mock(SiteInfo.class));
    when(syncUserInfoUseCase.syncActualUser("teacher")).thenReturn(userInfo);
    when(sessionPort.generateSession(eq(authInput), isNull(), eq(restClient)))
        .thenReturn(sessionToken);

    AuthService authService =
        new AuthService(
            syncUserInfoUseCase,
            mock(GetUserInfoPersistenceAdapterPort.class),
            siteInfoApiPort,
            mock(SiteInfoPersistencePort.class),
            sessionPort,
            moodleApiPort,
            tenantDatabasePort,
            asyncOperations);

    authService.loginByCredentials(authInput);

    verify(syncUserInfoUseCase).syncActualUser("teacher");
    verify(sessionPort).generateSession(eq(authInput), isNull(), eq(restClient));
    verify(sessionPort).updateSession(authInput, sessionToken, userInfo, restClient);
  }
}
