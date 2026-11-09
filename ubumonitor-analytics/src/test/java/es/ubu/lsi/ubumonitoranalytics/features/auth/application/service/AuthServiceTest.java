package es.ubu.lsi.ubumonitoranalytics.features.auth.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.in.SearchUserByUserNameUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.in.SyncUserInfoUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.out.GetUserInfoPersistenceAdapterPort;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.UnauthorizedException;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.database.TenantDatabasePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.DatabaseCreationException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.ActualUser;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.concurrent.AsyncOperations;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.web.client.RestClient;

class AuthServiceTest {

  @Test
  void loginByCredentialsReusesMatchingSessionOnlyAfterValidatingSiteInfo() {
    SessionPort sessionPort = mock(SessionPort.class);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    SiteInfoApiPort siteInfoApiPort = mock(SiteInfoApiPort.class);
    SiteInfoPersistencePort siteInfoPersistencePort = mock(SiteInfoPersistencePort.class);
    SearchUserByUserNameUseCase searchUserByUserNameUseCase =
        mock(SearchUserByUserNameUseCase.class);
    SyncUserInfoUseCase syncUserInfoUseCase = mock(SyncUserInfoUseCase.class);
    AuthInput authInput =
        new AuthInput(
            "teacher",
            "moodle-password",
            URI.create("https://moodle.test"),
            "database-password",
            null,
            null);
    SessionToken sessionToken = SessionToken.builder().token("unused-new-token").build();
    Site site = new Site();
    site.setUserId(13L);
    site.setUserName("teacher");
    site.setFirstName("Updated");
    site.setFullName("Updated Name");
    SiteInfo siteInfo = SiteInfo.builder().site(site).build();
    SessionData existingSession = storedSession(authInput, 13L);
    RestClient restClient = mock(RestClient.class);

    when(moodleApiPort.login(authInput)).thenReturn("moodle-token");
    when(moodleApiPort.loginWeb("teacher", "moodle-password", authInput.getHost()))
        .thenReturn(restClient);
    when(siteInfoApiPort.fetchSiteInfo()).thenReturn(siteInfo);
    when(sessionPort.generateSessionWithStatus(authInput, null, restClient))
        .thenReturn(new SessionPort.SessionGeneration(sessionToken, false));
    when(sessionPort.findReusableOfflineSession(authInput)).thenReturn(existingSession);

    AuthService authService =
        new AuthService(
            searchUserByUserNameUseCase,
            syncUserInfoUseCase,
            mock(GetUserInfoPersistenceAdapterPort.class),
            siteInfoApiPort,
            siteInfoPersistencePort,
            sessionPort,
            moodleApiPort,
            mock(TenantDatabasePort.class),
            new AsyncOperations(Runnable::run));

    SessionToken result = authService.loginByCredentials(authInput);

    assertEquals("reused-session-token", result.getToken());
    assertEquals("Stored Name", result.getCurrentUser().getFullName());
    InOrder inOrder = inOrder(siteInfoApiPort, sessionPort);
    inOrder.verify(siteInfoApiPort).fetchSiteInfo();
    inOrder.verify(sessionPort).findReusableOfflineSession(authInput);
    verify(siteInfoPersistencePort).save(siteInfo);
    verify(sessionPort, never()).updateSessionData(any(), any(), any(), any());
    verify(searchUserByUserNameUseCase).searchUserByUserName("teacher");
    verifyNoInteractions(syncUserInfoUseCase);
  }

  @Test
  void loginBySSOReusesMatchingSessionAfterValidatingSiteInfo() {
    SessionPort sessionPort = mock(SessionPort.class);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    SiteInfoApiPort siteInfoApiPort = mock(SiteInfoApiPort.class);
    SiteInfoPersistencePort siteInfoPersistencePort = mock(SiteInfoPersistencePort.class);
    SearchUserByUserNameUseCase searchUserByUserNameUseCase =
        mock(SearchUserByUserNameUseCase.class);
    SyncUserInfoUseCase syncUserInfoUseCase = mock(SyncUserInfoUseCase.class);
    AuthInput authInput =
        new AuthInput(
            null,
            null,
            URI.create("https://moodle.test"),
            "database-password",
            "moodle-token",
            null);
    SessionToken provisionalSession = SessionToken.builder().token("provisional-sso-token").build();
    Site site = new Site();
    site.setUserId(13L);
    site.setUserName("teacher");
    site.setFullName("Updated Name");
    SiteInfo siteInfo = SiteInfo.builder().site(site).build();
    SessionData existingSession = storedSession(authInput, 13L);

    when(sessionPort.generateSession(authInput, null, null)).thenReturn(provisionalSession);
    when(siteInfoApiPort.fetchSiteInfo()).thenReturn(siteInfo);
    when(sessionPort.findReusableOfflineSession(authInput)).thenReturn(existingSession);

    AuthService authService =
        new AuthService(
            searchUserByUserNameUseCase,
            syncUserInfoUseCase,
            mock(GetUserInfoPersistenceAdapterPort.class),
            siteInfoApiPort,
            siteInfoPersistencePort,
            sessionPort,
            moodleApiPort,
            mock(TenantDatabasePort.class),
            new AsyncOperations(Runnable::run));

    SessionToken result = authService.loginBySSO(authInput);

    assertEquals("reused-session-token", result.getToken());
    assertEquals("Stored Name", result.getCurrentUser().getFullName());
    InOrder inOrder = inOrder(siteInfoApiPort, sessionPort);
    inOrder.verify(siteInfoApiPort).fetchSiteInfo();
    inOrder.verify(sessionPort).findReusableOfflineSession(authInput);
    verify(sessionPort).invalidateSession(provisionalSession);
    verify(siteInfoPersistencePort).save(siteInfo);
    verifyNoInteractions(searchUserByUserNameUseCase, syncUserInfoUseCase, moodleApiPort);
  }

  @Test
  void loginOfflineUpdatesSessionDataWithoutReplacingItsToken() {
    SessionPort sessionPort = mock(SessionPort.class);
    GetUserInfoPersistenceAdapterPort userInfoPersistencePort =
        mock(GetUserInfoPersistenceAdapterPort.class);
    TenantDatabasePort tenantDatabasePort = mock(TenantDatabasePort.class);
    AuthInput authInput =
        new AuthInput(
            "teacher", null, URI.create("https://moodle.test"), "database-password", null, null);
    UserInfo userInfo = mock(UserInfo.class);
    SessionToken sessionToken = mock(SessionToken.class);
    AuthService authService =
        new AuthService(
            mock(SearchUserByUserNameUseCase.class),
            mock(SyncUserInfoUseCase.class),
            userInfoPersistencePort,
            mock(SiteInfoApiPort.class),
            mock(SiteInfoPersistencePort.class),
            sessionPort,
            mock(MoodleApiPort.class),
            tenantDatabasePort,
            new AsyncOperations(Runnable::run));
    when(userInfoPersistencePort.getActualUser("teacher")).thenReturn(userInfo);
    when(sessionPort.generateSession(authInput, null, null)).thenReturn(sessionToken);

    authService.loginOffline(authInput);

    verify(tenantDatabasePort).initializeTenant(any());
    verify(sessionPort).generateSession(authInput, null, null);
    verify(sessionPort).updateSessionData(authInput, sessionToken, userInfo, null);
    verify(sessionPort, never()).updateSession(any(), any(), any(), any());
  }

  private static SessionData storedSession(AuthInput authInput, long userId) {
    ActualUser actualUser = new ActualUser();
    actualUser.setFirstName("Stored");
    actualUser.setLastName("Name");
    actualUser.setFullName("Stored Name");
    return SessionData.builder()
        .token("reused-session-token")
        .host(authInput.getHost())
        .username("teacher")
        .dbPassword(authInput.getDbPassword())
        .userId(userId)
        .actualUser(actualUser)
        .build();
  }

  @Test
  void loginBySSOResolvesTheMoodleUserAndSynchronizesUserInfo() {
    SessionPort sessionPort = mock(SessionPort.class);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    TenantDatabasePort tenantDatabasePort = mock(TenantDatabasePort.class);
    SyncUserInfoUseCase syncUserInfoUseCase = mock(SyncUserInfoUseCase.class);
    SearchUserByUserNameUseCase searchUserByUserNameUseCase =
        mock(SearchUserByUserNameUseCase.class);
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
    SessionToken sessionToken = SessionToken.builder().token("existing-sso-token").build();
    SessionToken provisionalSessionToken =
        SessionToken.builder().token("provisional-sso-token").build();
    RestClient restClient = mock(RestClient.class);
    UserInfo userInfo = mock(UserInfo.class);
    Site site = new Site();
    site.setUserId(13L);
    site.setUserName("teacher");
    SiteInfo siteInfo = SiteInfo.builder().site(site).build();
    AsyncOperations asyncOperations = new AsyncOperations(Runnable::run);

    when(sessionPort.generateSession(authInput, null, null)).thenReturn(provisionalSessionToken);
    when(sessionPort.generateSessionWithStatus(authInput, null, restClient))
        .thenReturn(new SessionPort.SessionGeneration(sessionToken, false));
    when(siteInfoApiPort.fetchSiteInfo()).thenReturn(siteInfo);
    when(moodleApiPort.getRestClientFromCookies(authInput)).thenReturn(restClient);
    when(searchUserByUserNameUseCase.searchUserByUserName("teacher")).thenReturn(userInfo);
    when(syncUserInfoUseCase.syncActualUser(userInfo)).thenReturn(userInfo);

    AuthService authService =
        new AuthService(
            searchUserByUserNameUseCase,
            syncUserInfoUseCase,
            mock(GetUserInfoPersistenceAdapterPort.class),
            siteInfoApiPort,
            siteInfoPersistencePort,
            sessionPort,
            moodleApiPort,
            tenantDatabasePort,
            asyncOperations);

    SessionToken result = authService.loginBySSO(authInput);

    assertEquals("existing-sso-token", result.getToken());
    verify(tenantDatabasePort).initializeTenant(any());
    verify(sessionPort).generateSession(authInput, null, null);
    verify(sessionPort).invalidateSession(provisionalSessionToken);
    verify(sessionPort).generateSessionWithStatus(authInput, null, restClient);
    verify(siteInfoPersistencePort).save(siteInfo);
    verify(searchUserByUserNameUseCase).searchUserByUserName("teacher");
    verify(syncUserInfoUseCase).syncActualUser(userInfo);
    verify(sessionPort).updateSessionData(authInput, sessionToken, userInfo, restClient);
  }

  @Test
  void loginBySSOCreatesSessionAfterResolvingUserWhenNoSessionExists() {
    SessionPort sessionPort = mock(SessionPort.class);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    SiteInfoApiPort siteInfoApiPort = mock(SiteInfoApiPort.class);
    SyncUserInfoUseCase syncUserInfoUseCase = mock(SyncUserInfoUseCase.class);
    SearchUserByUserNameUseCase searchUserByUserNameUseCase =
        mock(SearchUserByUserNameUseCase.class);
    AuthInput authInput =
        new AuthInput(
            null,
            null,
            URI.create("https://moodle.test"),
            "database-password",
            "moodle-token",
            null);
    SessionToken sessionToken = SessionToken.builder().token("new-sso-token").build();
    SessionToken provisionalSessionToken =
        SessionToken.builder().token("provisional-sso-token").build();
    RestClient restClient = mock(RestClient.class);
    UserInfo userInfo = mock(UserInfo.class);
    Site site = new Site();
    site.setUserId(13L);
    site.setUserName("teacher");

    when(siteInfoApiPort.fetchSiteInfo()).thenReturn(SiteInfo.builder().site(site).build());
    when(moodleApiPort.getRestClientFromCookies(authInput)).thenReturn(restClient);
    when(sessionPort.generateSession(authInput, null, null)).thenReturn(provisionalSessionToken);
    when(sessionPort.generateSessionWithStatus(any(), isNull(), eq(restClient)))
        .thenAnswer(
            invocation -> {
              AuthInput sessionAuthInput = invocation.getArgument(0);
              assertEquals("teacher", sessionAuthInput.getUsername());
              return new SessionPort.SessionGeneration(sessionToken, true);
            });
    when(searchUserByUserNameUseCase.searchUserByUserName("teacher")).thenReturn(userInfo);
    when(syncUserInfoUseCase.syncActualUser(userInfo)).thenReturn(userInfo);

    AuthService authService =
        new AuthService(
            searchUserByUserNameUseCase,
            syncUserInfoUseCase,
            mock(GetUserInfoPersistenceAdapterPort.class),
            siteInfoApiPort,
            mock(SiteInfoPersistencePort.class),
            sessionPort,
            moodleApiPort,
            mock(TenantDatabasePort.class),
            new AsyncOperations(Runnable::run));

    SessionToken result = authService.loginBySSO(authInput);

    assertEquals("new-sso-token", result.getToken());
    verify(sessionPort).generateSession(authInput, null, null);
    verify(sessionPort).invalidateSession(provisionalSessionToken);
    verify(sessionPort).generateSessionWithStatus(authInput, null, restClient);
    verify(sessionPort).updateSessionData(authInput, sessionToken, userInfo, restClient);
  }

  @Test
  void invalidSSOUserInvalidatesTheProvisionalSession() {
    SessionPort sessionPort = mock(SessionPort.class);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    SiteInfoApiPort siteInfoApiPort = mock(SiteInfoApiPort.class);
    SessionToken provisionalSessionToken =
        SessionToken.builder().token("provisional-sso-token").build();
    when(sessionPort.generateSession(any(), isNull(), isNull()))
        .thenReturn(provisionalSessionToken);
    when(siteInfoApiPort.fetchSiteInfo()).thenReturn(new SiteInfo());

    AuthService authService =
        new AuthService(
            mock(SearchUserByUserNameUseCase.class),
            mock(SyncUserInfoUseCase.class),
            mock(GetUserInfoPersistenceAdapterPort.class),
            siteInfoApiPort,
            mock(SiteInfoPersistencePort.class),
            sessionPort,
            moodleApiPort,
            mock(TenantDatabasePort.class),
            new AsyncOperations(Runnable::run));

    assertThrows(UnauthorizedException.class, () -> authService.loginBySSO(new AuthInput()));

    verify(sessionPort).generateSession(any(), isNull(), isNull());
    verify(sessionPort).invalidateSession(provisionalSessionToken);
    verifyNoInteractions(moodleApiPort);
  }

  @Test
  void loginBySSODoesNotInvalidateAnExistingSessionWhenLaterProcessingFails() {
    SessionPort sessionPort = mock(SessionPort.class);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    SiteInfoApiPort siteInfoApiPort = mock(SiteInfoApiPort.class);
    SyncUserInfoUseCase syncUserInfoUseCase = mock(SyncUserInfoUseCase.class);
    SearchUserByUserNameUseCase searchUserByUserNameUseCase =
        mock(SearchUserByUserNameUseCase.class);
    AuthInput authInput =
        new AuthInput(
            null,
            null,
            URI.create("https://moodle.test"),
            "database-password",
            "moodle-token",
            null);
    SessionToken provisionalSessionToken =
        SessionToken.builder().token("provisional-sso-token").build();
    SessionToken existingSessionToken = SessionToken.builder().token("existing-sso-token").build();
    RestClient restClient = mock(RestClient.class);
    Site site = new Site();
    site.setUserName("teacher");

    when(sessionPort.generateSession(authInput, null, null)).thenReturn(provisionalSessionToken);
    when(siteInfoApiPort.fetchSiteInfo()).thenReturn(SiteInfo.builder().site(site).build());
    when(moodleApiPort.getRestClientFromCookies(authInput)).thenReturn(restClient);
    when(sessionPort.generateSessionWithStatus(authInput, null, restClient))
        .thenReturn(new SessionPort.SessionGeneration(existingSessionToken, false));
    UserInfo userInfo = mock(UserInfo.class);
    when(searchUserByUserNameUseCase.searchUserByUserName("teacher")).thenReturn(userInfo);
    when(syncUserInfoUseCase.syncActualUser(userInfo))
        .thenThrow(new IllegalStateException("User synchronization failed"));

    AuthService authService =
        new AuthService(
            searchUserByUserNameUseCase,
            syncUserInfoUseCase,
            mock(GetUserInfoPersistenceAdapterPort.class),
            siteInfoApiPort,
            mock(SiteInfoPersistencePort.class),
            sessionPort,
            moodleApiPort,
            mock(TenantDatabasePort.class),
            new AsyncOperations(Runnable::run));

    assertThrows(IllegalStateException.class, () -> authService.loginBySSO(authInput));

    verify(sessionPort).invalidateSession(provisionalSessionToken);
    verify(sessionPort, never()).invalidateSession(existingSessionToken);
  }

  @Test
  void loginBySSOCleansUpNewSessionWhenLaterProcessingFails() {
    SessionPort sessionPort = mock(SessionPort.class);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    SiteInfoApiPort siteInfoApiPort = mock(SiteInfoApiPort.class);
    SyncUserInfoUseCase syncUserInfoUseCase = mock(SyncUserInfoUseCase.class);
    SearchUserByUserNameUseCase searchUserByUserNameUseCase =
        mock(SearchUserByUserNameUseCase.class);
    AuthInput authInput =
        new AuthInput(
            null,
            null,
            URI.create("https://moodle.test"),
            "database-password",
            "moodle-token",
            null);
    SessionToken provisionalSessionToken =
        SessionToken.builder().token("provisional-sso-token").build();
    SessionToken newSessionToken = SessionToken.builder().token("new-sso-token").build();
    RestClient restClient = mock(RestClient.class);
    Site site = new Site();
    site.setUserName("teacher");

    when(sessionPort.generateSession(authInput, null, null)).thenReturn(provisionalSessionToken);
    when(siteInfoApiPort.fetchSiteInfo()).thenReturn(SiteInfo.builder().site(site).build());
    when(moodleApiPort.getRestClientFromCookies(authInput)).thenReturn(restClient);
    when(sessionPort.generateSessionWithStatus(authInput, null, restClient))
        .thenReturn(new SessionPort.SessionGeneration(newSessionToken, true));
    UserInfo userInfo = mock(UserInfo.class);
    when(searchUserByUserNameUseCase.searchUserByUserName("teacher")).thenReturn(userInfo);
    when(syncUserInfoUseCase.syncActualUser(userInfo))
        .thenThrow(new IllegalStateException("User synchronization failed"));

    AuthService authService =
        new AuthService(
            searchUserByUserNameUseCase,
            syncUserInfoUseCase,
            mock(GetUserInfoPersistenceAdapterPort.class),
            siteInfoApiPort,
            mock(SiteInfoPersistencePort.class),
            sessionPort,
            moodleApiPort,
            mock(TenantDatabasePort.class),
            new AsyncOperations(Runnable::run));

    assertThrows(IllegalStateException.class, () -> authService.loginBySSO(authInput));

    verify(sessionPort).invalidateSession(provisionalSessionToken);
    verify(sessionPort).invalidateSession(newSessionToken);
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
            mock(SearchUserByUserNameUseCase.class),
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
            mock(SearchUserByUserNameUseCase.class),
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
            mock(SearchUserByUserNameUseCase.class),
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
    SearchUserByUserNameUseCase searchUserByUserNameUseCase =
        mock(SearchUserByUserNameUseCase.class);
    SiteInfoApiPort siteInfoApiPort = mock(SiteInfoApiPort.class);
    UserInfo userInfo = mock(UserInfo.class);
    SessionToken sessionToken = SessionToken.builder().token("existing-credential-token").build();
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
    Site site = new Site();
    site.setUserId(13L);
    site.setUserName("teacher");
    when(siteInfoApiPort.fetchSiteInfo()).thenReturn(SiteInfo.builder().site(site).build());
    when(searchUserByUserNameUseCase.searchUserByUserName("teacher")).thenReturn(userInfo);
    when(syncUserInfoUseCase.syncActualUser(userInfo)).thenReturn(userInfo);
    when(sessionPort.generateSessionWithStatus(eq(authInput), isNull(), eq(restClient)))
        .thenReturn(new SessionPort.SessionGeneration(sessionToken, false));

    AuthService authService =
        new AuthService(
            searchUserByUserNameUseCase,
            syncUserInfoUseCase,
            mock(GetUserInfoPersistenceAdapterPort.class),
            siteInfoApiPort,
            mock(SiteInfoPersistencePort.class),
            sessionPort,
            moodleApiPort,
            tenantDatabasePort,
            asyncOperations);

    SessionToken result = authService.loginByCredentials(authInput);

    assertEquals("existing-credential-token", result.getToken());
    verify(tenantDatabasePort).initializeTenant(any());
    verify(sessionPort).generateSessionWithStatus(eq(authInput), isNull(), eq(restClient));
    verify(sessionPort).updateSessionData(authInput, sessionToken, userInfo, restClient);
    verify(searchUserByUserNameUseCase).searchUserByUserName("teacher");
    verify(syncUserInfoUseCase).syncActualUser(userInfo);
  }

  @Test
  void synchronizesUserInfoWhenTenantDatabaseAlreadyExists() {
    SessionPort sessionPort = mock(SessionPort.class);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    TenantDatabasePort tenantDatabasePort = mock(TenantDatabasePort.class);
    SyncUserInfoUseCase syncUserInfoUseCase = mock(SyncUserInfoUseCase.class);
    SearchUserByUserNameUseCase searchUserByUserNameUseCase =
        mock(SearchUserByUserNameUseCase.class);
    SiteInfoApiPort siteInfoApiPort = mock(SiteInfoApiPort.class);
    UserInfo userInfo = mock(UserInfo.class);
    SessionToken sessionToken = SessionToken.builder().token("existing-credential-token").build();
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
    Site site = new Site();
    site.setUserId(13L);
    site.setUserName("teacher");
    when(siteInfoApiPort.fetchSiteInfo()).thenReturn(SiteInfo.builder().site(site).build());
    when(searchUserByUserNameUseCase.searchUserByUserName("teacher")).thenReturn(userInfo);
    when(syncUserInfoUseCase.syncActualUser(userInfo)).thenReturn(userInfo);
    when(sessionPort.generateSessionWithStatus(eq(authInput), isNull(), eq(restClient)))
        .thenReturn(new SessionPort.SessionGeneration(sessionToken, false));

    AuthService authService =
        new AuthService(
            searchUserByUserNameUseCase,
            syncUserInfoUseCase,
            mock(GetUserInfoPersistenceAdapterPort.class),
            siteInfoApiPort,
            mock(SiteInfoPersistencePort.class),
            sessionPort,
            moodleApiPort,
            tenantDatabasePort,
            asyncOperations);

    SessionToken result = authService.loginByCredentials(authInput);

    assertEquals("existing-credential-token", result.getToken());
    verify(searchUserByUserNameUseCase).searchUserByUserName("teacher");
    verify(syncUserInfoUseCase).syncActualUser(userInfo);
    verify(sessionPort).generateSessionWithStatus(eq(authInput), isNull(), eq(restClient));
    verify(sessionPort).updateSessionData(authInput, sessionToken, userInfo, restClient);
  }

  @Test
  void loginByCredentialsChecksForReusableSessionAfterFetchingSiteInfo() {
    SessionPort sessionPort = mock(SessionPort.class);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    SyncUserInfoUseCase syncUserInfoUseCase = mock(SyncUserInfoUseCase.class);
    SearchUserByUserNameUseCase searchUserByUserNameUseCase =
        mock(SearchUserByUserNameUseCase.class);
    SiteInfoApiPort siteInfoApiPort = mock(SiteInfoApiPort.class);
    SiteInfoPersistencePort siteInfoPersistencePort = mock(SiteInfoPersistencePort.class);
    UserInfo userInfo = mock(UserInfo.class);
    SessionToken sessionToken = SessionToken.builder().token("credential-token").build();
    RestClient restClient = mock(RestClient.class);
    AuthInput authInput =
        new AuthInput(
            "teacher",
            "moodle-password",
            URI.create("https://moodle.test"),
            "database-password",
            null,
            null);
    Site site = new Site();
    site.setUserId(13L);
    site.setUserName("teacher");
    SiteInfo siteInfo = SiteInfo.builder().site(site).build();
    when(moodleApiPort.login(authInput)).thenReturn("moodle-token");
    when(moodleApiPort.loginWeb("teacher", "moodle-password", authInput.getHost()))
        .thenReturn(restClient);
    when(siteInfoApiPort.fetchSiteInfo()).thenReturn(siteInfo);
    when(searchUserByUserNameUseCase.searchUserByUserName("teacher")).thenReturn(userInfo);
    when(syncUserInfoUseCase.syncActualUser(userInfo)).thenReturn(userInfo);
    when(sessionPort.generateSessionWithStatus(eq(authInput), isNull(), eq(restClient)))
        .thenReturn(new SessionPort.SessionGeneration(sessionToken, false));

    AuthService authService =
        new AuthService(
            searchUserByUserNameUseCase,
            syncUserInfoUseCase,
            mock(GetUserInfoPersistenceAdapterPort.class),
            siteInfoApiPort,
            siteInfoPersistencePort,
            sessionPort,
            moodleApiPort,
            mock(TenantDatabasePort.class),
            new AsyncOperations(Runnable::run));

    SessionToken result = authService.loginByCredentials(authInput);

    assertEquals("credential-token", result.getToken());
    InOrder inOrder = inOrder(siteInfoApiPort, sessionPort);
    inOrder.verify(siteInfoApiPort).fetchSiteInfo();
    inOrder.verify(sessionPort).findReusableOfflineSession(authInput);
    verify(searchUserByUserNameUseCase).searchUserByUserName("teacher");
    verify(syncUserInfoUseCase).syncActualUser(userInfo);
    verify(siteInfoPersistencePort).save(siteInfo);
  }

  @Test
  void loginByCredentialsFetchesSiteInfoAndSearchesUserConcurrently() {
    SessionPort sessionPort = mock(SessionPort.class);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    SiteInfoApiPort siteInfoApiPort = mock(SiteInfoApiPort.class);
    SearchUserByUserNameUseCase searchUserByUserNameUseCase =
        mock(SearchUserByUserNameUseCase.class);
    SyncUserInfoUseCase syncUserInfoUseCase = mock(SyncUserInfoUseCase.class);
    CountDownLatch concurrentCallsStarted = new CountDownLatch(2);
    ExecutorService executor = Executors.newFixedThreadPool(2);
    AuthInput authInput =
        new AuthInput(
            "teacher",
            "moodle-password",
            URI.create("https://moodle.test"),
            "database-password",
            null,
            null);
    RestClient restClient = mock(RestClient.class);
    SessionToken sessionToken = SessionToken.builder().token("credential-token").build();
    UserInfo userInfo = mock(UserInfo.class);
    Site site = new Site();
    site.setUserId(13L);
    site.setUserName("teacher");
    SiteInfo siteInfo = SiteInfo.builder().site(site).build();

    when(moodleApiPort.login(authInput)).thenReturn("moodle-token");
    when(moodleApiPort.loginWeb("teacher", "moodle-password", authInput.getHost()))
        .thenReturn(restClient);
    when(siteInfoApiPort.fetchSiteInfo())
        .thenAnswer(
            invocation -> {
              awaitConcurrentCall(concurrentCallsStarted);
              return siteInfo;
            });
    when(searchUserByUserNameUseCase.searchUserByUserName("teacher"))
        .thenAnswer(
            invocation -> {
              awaitConcurrentCall(concurrentCallsStarted);
              return userInfo;
            });
    when(syncUserInfoUseCase.syncActualUser(userInfo)).thenReturn(userInfo);
    when(sessionPort.generateSessionWithStatus(authInput, null, restClient))
        .thenReturn(new SessionPort.SessionGeneration(sessionToken, true));

    AuthService authService =
        new AuthService(
            searchUserByUserNameUseCase,
            syncUserInfoUseCase,
            mock(GetUserInfoPersistenceAdapterPort.class),
            siteInfoApiPort,
            mock(SiteInfoPersistencePort.class),
            sessionPort,
            moodleApiPort,
            mock(TenantDatabasePort.class),
            new AsyncOperations(executor));

    try {
      SessionToken result = authService.loginByCredentials(authInput);
      assertEquals("credential-token", result.getToken());
      verify(searchUserByUserNameUseCase).searchUserByUserName("teacher");
      verify(syncUserInfoUseCase).syncActualUser(userInfo);
      verify(sessionPort).updateSessionData(authInput, sessionToken, userInfo, restClient);
    } finally {
      executor.shutdownNow();
    }
  }

  private static void awaitConcurrentCall(CountDownLatch concurrentCallsStarted) {
    concurrentCallsStarted.countDown();
    try {
      assertTrue(concurrentCallsStarted.await(2, TimeUnit.SECONDS));
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new AssertionError(exception);
    }
  }

  @Test
  void loginByCredentialsDoesNotInvalidateAnExistingSessionWhenLaterProcessingFails() {
    SessionPort sessionPort = mock(SessionPort.class);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    SyncUserInfoUseCase syncUserInfoUseCase = mock(SyncUserInfoUseCase.class);
    SearchUserByUserNameUseCase searchUserByUserNameUseCase =
        mock(SearchUserByUserNameUseCase.class);
    SiteInfoApiPort siteInfoApiPort = mock(SiteInfoApiPort.class);
    SessionToken existingSessionToken =
        SessionToken.builder().token("existing-credential-token").build();
    RestClient restClient = mock(RestClient.class);
    AuthInput authInput =
        new AuthInput(
            "teacher",
            "moodle-password",
            URI.create("https://moodle.test"),
            "database-password",
            null,
            null);
    Site site = new Site();
    site.setUserId(13L);
    site.setUserName("teacher");

    when(moodleApiPort.login(authInput)).thenReturn("moodle-token");
    when(moodleApiPort.loginWeb("teacher", "moodle-password", authInput.getHost()))
        .thenReturn(restClient);
    when(siteInfoApiPort.fetchSiteInfo()).thenReturn(SiteInfo.builder().site(site).build());
    when(sessionPort.generateSessionWithStatus(eq(authInput), isNull(), eq(restClient)))
        .thenReturn(new SessionPort.SessionGeneration(existingSessionToken, false));
    UserInfo userInfo = mock(UserInfo.class);
    when(searchUserByUserNameUseCase.searchUserByUserName("teacher")).thenReturn(userInfo);
    when(syncUserInfoUseCase.syncActualUser(userInfo))
        .thenThrow(new IllegalStateException("User synchronization failed"));

    AuthService authService =
        new AuthService(
            searchUserByUserNameUseCase,
            syncUserInfoUseCase,
            mock(GetUserInfoPersistenceAdapterPort.class),
            siteInfoApiPort,
            mock(SiteInfoPersistencePort.class),
            sessionPort,
            moodleApiPort,
            mock(TenantDatabasePort.class),
            new AsyncOperations(Runnable::run));

    assertThrows(IllegalStateException.class, () -> authService.loginByCredentials(authInput));

    verify(searchUserByUserNameUseCase).searchUserByUserName("teacher");
    verify(syncUserInfoUseCase).syncActualUser(userInfo);
    verify(sessionPort, never()).invalidateSession(existingSessionToken);
  }
}
