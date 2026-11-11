package es.ubu.lsi.ubumonitoranalytics.features.auth.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
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
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.concurrent.AsyncOperations;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.web.client.RestClient;

class AuthServiceTest {

  @Test
  void loginByCredentialsCreatesFreshSessionAfterValidatingAndSynchronizingUser() {
    SessionPort sessionPort = mock(SessionPort.class);
    stubAuthenticationContext(sessionPort);
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
    SessionToken sessionToken = SessionToken.builder().token("new-credential-token").build();
    Site site = new Site();
    site.setUserId(13L);
    site.setUserName("teacher");
    site.setFirstName("Updated");
    site.setFullName("Updated Name");
    SiteInfo siteInfo = SiteInfo.builder().site(site).build();
    RestClient restClient = mock(RestClient.class);

    when(moodleApiPort.login(authInput)).thenReturn("moodle-token");
    when(moodleApiPort.loginWeb("teacher", "moodle-password", authInput.getHost()))
        .thenReturn(restClient);
    when(siteInfoApiPort.fetchSiteInfo()).thenReturn(siteInfo);
    UserInfo userInfo = mock(UserInfo.class);
    when(searchUserByUserNameUseCase.searchUserByUserName("teacher")).thenReturn(userInfo);
    when(syncUserInfoUseCase.syncActualUser(userInfo)).thenReturn(userInfo);
    when(sessionPort.generateNewSession(authInput, userInfo, restClient)).thenReturn(sessionToken);

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
            new AsyncOperations(Runnable::run),
            new CurrentSessionContext());

    SessionToken result = authService.loginByCredentials(authInput);

    assertEquals("new-credential-token", result.getToken());
    assertEquals("Updated Name", result.getCurrentUser().getFullName());
    InOrder inOrder = inOrder(siteInfoApiPort, sessionPort, syncUserInfoUseCase);
    inOrder.verify(siteInfoApiPort).fetchSiteInfo();
    inOrder.verify(syncUserInfoUseCase).syncActualUser(userInfo);
    inOrder.verify(sessionPort).generateNewSession(authInput, userInfo, restClient);
    verify(siteInfoPersistencePort).save(siteInfo);
    verify(searchUserByUserNameUseCase).searchUserByUserName("teacher");
  }

  @Test
  void loginBySSOCreatesFreshSessionEvenWhenUserAlreadyHasASession() {
    SessionPort sessionPort = mock(SessionPort.class);
    stubAuthenticationContext(sessionPort);
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
    SessionToken newSession = SessionToken.builder().token("new-sso-token").build();
    Site site = new Site();
    site.setUserId(13L);
    site.setUserName("teacher");
    site.setFullName("Updated Name");
    SiteInfo siteInfo = SiteInfo.builder().site(site).build();
    RestClient restClient = mock(RestClient.class);
    UserInfo userInfo = mock(UserInfo.class);

    when(siteInfoApiPort.fetchSiteInfo()).thenReturn(siteInfo);
    when(moodleApiPort.getRestClientFromCookies(authInput)).thenReturn(restClient);
    when(searchUserByUserNameUseCase.searchUserByUserName("teacher")).thenReturn(userInfo);
    when(syncUserInfoUseCase.syncActualUser(userInfo)).thenReturn(userInfo);
    when(sessionPort.generateNewSession(authInput, userInfo, restClient)).thenReturn(newSession);

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
            new AsyncOperations(Runnable::run),
            new CurrentSessionContext());

    SessionToken result = authService.loginBySSO(authInput);

    assertEquals("new-sso-token", result.getToken());
    InOrder inOrder = inOrder(siteInfoApiPort, syncUserInfoUseCase, sessionPort);
    inOrder.verify(siteInfoApiPort).fetchSiteInfo();
    inOrder.verify(syncUserInfoUseCase).syncActualUser(userInfo);
    inOrder.verify(sessionPort).generateNewSession(authInput, userInfo, restClient);
    verify(siteInfoPersistencePort).save(siteInfo);
    verify(searchUserByUserNameUseCase).searchUserByUserName("teacher");
  }

  @Test
  void loginOfflineCreatesNewSessionAfterLoadingUserInfo() {
    SessionPort sessionPort = mock(SessionPort.class);
    stubAuthenticationContext(sessionPort);
    GetUserInfoPersistenceAdapterPort userInfoPersistencePort =
        mock(GetUserInfoPersistenceAdapterPort.class);
    TenantDatabasePort tenantDatabasePort = mock(TenantDatabasePort.class);
    AuthInput authInput =
        new AuthInput(
            "teacher", null, URI.create("https://moodle.test"), "database-password", null, null);
    UserInfo userInfo = mock(UserInfo.class);
    SessionToken sessionToken = SessionToken.builder().token("offline-token").build();
    SessionData authSessionData =
        SessionData.builder()
            .host(authInput.getHost())
            .username(authInput.getUsername())
            .dbPassword(authInput.getDbPassword())
            .build();
    CurrentSessionContext currentSessionContext = new CurrentSessionContext();
    when(sessionPort.buildAuthenticationContext(authInput, null)).thenReturn(authSessionData);
    doAnswer(
            invocation -> {
              assertEquals(authSessionData, currentSessionContext.getSessionData());
              return false;
            })
        .when(tenantDatabasePort)
        .initializeTenant(any());
    when(userInfoPersistencePort.getActualUser("teacher"))
        .thenAnswer(
            invocation -> {
              assertEquals(authSessionData, currentSessionContext.getSessionData());
              return userInfo;
            });
    when(sessionPort.generateNewSession(authInput, userInfo, null))
        .thenAnswer(
            invocation -> {
              assertEquals(authSessionData, currentSessionContext.getSessionData());
              return sessionToken;
            });
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
            new AsyncOperations(Runnable::run),
            currentSessionContext);
    InOrder inOrder = inOrder(userInfoPersistencePort, sessionPort);
    SessionToken result = authService.loginOffline(authInput);

    assertEquals("offline-token", result.getToken());
    inOrder.verify(userInfoPersistencePort).getActualUser("teacher");
    inOrder.verify(sessionPort).generateNewSession(authInput, userInfo, null);
    verify(tenantDatabasePort).initializeTenant(any());
    assertTenantSessionData(tenantDatabasePort, authInput, "teacher");
    verify(sessionPort).buildAuthenticationContext(authInput, null);
    verify(sessionPort).generateNewSession(authInput, userInfo, null);
  }

  @Test
  void loginBySSOResolvesTheMoodleUserAndSynchronizesUserInfo() {
    SessionPort sessionPort = mock(SessionPort.class);
    stubAuthenticationContext(sessionPort);
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
    SessionToken sessionToken = SessionToken.builder().token("new-sso-token").build();
    RestClient restClient = mock(RestClient.class);
    UserInfo userInfo = mock(UserInfo.class);
    Site site = new Site();
    site.setUserId(13L);
    site.setUserName("teacher");
    SiteInfo siteInfo = SiteInfo.builder().site(site).build();
    AsyncOperations asyncOperations = new AsyncOperations(Runnable::run);

    when(siteInfoApiPort.fetchSiteInfo()).thenReturn(siteInfo);
    when(moodleApiPort.getRestClientFromCookies(authInput)).thenReturn(restClient);
    when(searchUserByUserNameUseCase.searchUserByUserName("teacher")).thenReturn(userInfo);
    when(syncUserInfoUseCase.syncActualUser(userInfo)).thenReturn(userInfo);
    when(sessionPort.generateNewSession(authInput, userInfo, restClient)).thenReturn(sessionToken);

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
            asyncOperations,
            new CurrentSessionContext());

    SessionToken result = authService.loginBySSO(authInput);

    assertEquals("new-sso-token", result.getToken());
    verify(tenantDatabasePort).initializeTenant(any());
    assertTenantSessionData(tenantDatabasePort, authInput, "teacher");
    verify(sessionPort).buildAuthenticationContext(authInput, null);
    verify(sessionPort).buildAuthenticationContext(authInput, restClient);
    verify(sessionPort).generateNewSession(authInput, userInfo, restClient);
    verify(siteInfoPersistencePort).save(siteInfo);
    verify(searchUserByUserNameUseCase).searchUserByUserName("teacher");
    verify(syncUserInfoUseCase).syncActualUser(userInfo);
  }

  @Test
  void loginBySSOCreatesSessionAfterResolvingUserWhenNoSessionExists() {
    SessionPort sessionPort = mock(SessionPort.class);
    stubAuthenticationContext(sessionPort);
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
    RestClient restClient = mock(RestClient.class);
    UserInfo userInfo = mock(UserInfo.class);
    Site site = new Site();
    site.setUserId(13L);
    site.setUserName("teacher");

    when(siteInfoApiPort.fetchSiteInfo()).thenReturn(SiteInfo.builder().site(site).build());
    when(moodleApiPort.getRestClientFromCookies(authInput)).thenReturn(restClient);
    when(searchUserByUserNameUseCase.searchUserByUserName("teacher")).thenReturn(userInfo);
    when(syncUserInfoUseCase.syncActualUser(userInfo)).thenReturn(userInfo);
    when(sessionPort.generateNewSession(any(), eq(userInfo), eq(restClient)))
        .thenAnswer(
            invocation -> {
              AuthInput sessionAuthInput = invocation.getArgument(0);
              assertEquals("teacher", sessionAuthInput.getUsername());
              return sessionToken;
            });

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
            new AsyncOperations(Runnable::run),
            new CurrentSessionContext());

    SessionToken result = authService.loginBySSO(authInput);

    assertEquals("new-sso-token", result.getToken());
    verify(sessionPort).generateNewSession(authInput, userInfo, restClient);
  }

  @Test
  void invalidSSOUserInvalidatesTheProvisionalSession() {
    SessionPort sessionPort = mock(SessionPort.class);
    stubAuthenticationContext(sessionPort);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    SiteInfoApiPort siteInfoApiPort = mock(SiteInfoApiPort.class);
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
            new AsyncOperations(Runnable::run),
            new CurrentSessionContext());

    assertThrows(UnauthorizedException.class, () -> authService.loginBySSO(new AuthInput()));

    verify(sessionPort).buildAuthenticationContext(any(), isNull());
    verify(sessionPort, never()).generateNewSession(any(), any(), any());
    verifyNoInteractions(moodleApiPort);
  }

  @Test
  void loginBySSODoesNotInvalidateAnExistingSessionWhenLaterProcessingFails() {
    SessionPort sessionPort = mock(SessionPort.class);
    stubAuthenticationContext(sessionPort);
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
    SessionToken existingSessionToken = SessionToken.builder().token("existing-sso-token").build();
    RestClient restClient = mock(RestClient.class);
    Site site = new Site();
    site.setUserName("teacher");
    site.setUserId(13L);

    when(siteInfoApiPort.fetchSiteInfo()).thenReturn(SiteInfo.builder().site(site).build());
    when(moodleApiPort.getRestClientFromCookies(authInput)).thenReturn(restClient);
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
            new AsyncOperations(Runnable::run),
            new CurrentSessionContext());

    assertThrows(IllegalStateException.class, () -> authService.loginBySSO(authInput));

    verify(sessionPort, never()).generateNewSession(any(), any(), any());
  }

  @Test
  void loginBySSOCleansUpNewSessionWhenLaterProcessingFails() {
    SessionPort sessionPort = mock(SessionPort.class);
    stubAuthenticationContext(sessionPort);
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
    RestClient restClient = mock(RestClient.class);
    Site site = new Site();
    site.setUserName("teacher");
    site.setUserId(13L);

    when(siteInfoApiPort.fetchSiteInfo()).thenReturn(SiteInfo.builder().site(site).build());
    when(moodleApiPort.getRestClientFromCookies(authInput)).thenReturn(restClient);
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
            new AsyncOperations(Runnable::run),
            new CurrentSessionContext());

    assertThrows(IllegalStateException.class, () -> authService.loginBySSO(authInput));

    verify(sessionPort, never()).generateNewSession(any(), any(), any());
  }

  @Test
  void doesNotInitializeTenantDatabaseWhenMoodleRejectsCredentials() {
    SessionPort sessionPort = mock(SessionPort.class);
    stubAuthenticationContext(sessionPort);
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
            asyncOperations,
            new CurrentSessionContext());
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
    stubAuthenticationContext(sessionPort);
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
            asyncOperations,
            new CurrentSessionContext());

    assertThrows(UnauthorizedException.class, () -> authService.loginByCredentials(authInput));
    verify(moodleApiPort).login(authInput);
    verifyNoInteractions(tenantDatabasePort, sessionPort);
  }

  @Test
  void loginByCredentialsRejectsNullSiteInfoAndCancelsThePendingUserSearch() {
    assertCredentialsSiteInfoFailure(null);
  }

  @Test
  void loginByCredentialsRejectsMismatchedSiteUsernameAndCancelsThePendingUserSearch() {
    Site site = new Site();
    site.setUserId(13L);
    site.setUserName("another-user");
    assertCredentialsSiteInfoFailure(SiteInfo.builder().site(site).build());
  }

  @Test
  void loginByCredentialsInvalidatesTheGeneratedSessionWhenAttachingUserInfoFails() {
    SessionPort sessionPort = mock(SessionPort.class);
    stubAuthenticationContext(sessionPort);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    SiteInfoApiPort siteInfoApiPort = mock(SiteInfoApiPort.class);
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
    RestClient restClient = mock(RestClient.class);
    SessionToken generatedSession = SessionToken.builder().token("generated-token").build();
    Site site = new Site();
    site.setUserId(13L);
    site.setUserName("teacher");
    UserInfo userInfo = mock(UserInfo.class);

    when(moodleApiPort.login(authInput)).thenReturn("moodle-token");
    when(moodleApiPort.loginWeb("teacher", "moodle-password", authInput.getHost()))
        .thenReturn(restClient);
    when(siteInfoApiPort.fetchSiteInfo()).thenReturn(SiteInfo.builder().site(site).build());
    when(searchUserByUserNameUseCase.searchUserByUserName("teacher")).thenReturn(userInfo);
    when(syncUserInfoUseCase.syncActualUser(userInfo)).thenReturn(userInfo);
    when(sessionPort.generateNewSession(authInput, userInfo, restClient))
        .thenReturn(generatedSession);
    when(userInfo.getInitials()).thenThrow(new IllegalStateException("User info unavailable"));

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
            new AsyncOperations(Runnable::run),
            new CurrentSessionContext());

    assertThrows(IllegalStateException.class, () -> authService.loginByCredentials(authInput));

    verify(sessionPort).invalidateSession(generatedSession);
  }

  @Test
  void doesNotCreateSessionWhenTenantDatabasePasswordIsRejected() {
    SessionPort sessionPort = mock(SessionPort.class);
    stubAuthenticationContext(sessionPort);
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
            asyncOperations,
            new CurrentSessionContext());

    assertThrows(DatabaseCreationException.class, () -> authService.loginByCredentials(authInput));
    verify(tenantDatabasePort).initializeTenant(any());
    assertTenantSessionData(tenantDatabasePort, authInput, "teacher");
    verify(sessionPort).buildAuthenticationContext(eq(authInput), any());
    verify(sessionPort, never()).generateNewSession(any(), any(), any());
  }

  @Test
  void synchronizesUserInfoAfterCreatingTenantDatabase() {
    SessionPort sessionPort = mock(SessionPort.class);
    stubAuthenticationContext(sessionPort);
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
    when(sessionPort.generateNewSession(authInput, userInfo, restClient)).thenReturn(sessionToken);

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
            asyncOperations,
            new CurrentSessionContext());

    SessionToken result = authService.loginByCredentials(authInput);

    assertEquals("existing-credential-token", result.getToken());
    verify(tenantDatabasePort).initializeTenant(any());
    verify(sessionPort).generateNewSession(authInput, userInfo, restClient);
    verify(searchUserByUserNameUseCase).searchUserByUserName("teacher");
    verify(syncUserInfoUseCase).syncActualUser(userInfo);
  }

  @Test
  void synchronizesUserInfoWhenTenantDatabaseAlreadyExists() {
    SessionPort sessionPort = mock(SessionPort.class);
    stubAuthenticationContext(sessionPort);
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
    when(sessionPort.generateNewSession(authInput, userInfo, restClient)).thenReturn(sessionToken);

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
            asyncOperations,
            new CurrentSessionContext());

    SessionToken result = authService.loginByCredentials(authInput);

    assertEquals("existing-credential-token", result.getToken());
    verify(searchUserByUserNameUseCase).searchUserByUserName("teacher");
    verify(syncUserInfoUseCase).syncActualUser(userInfo);
    verify(sessionPort).generateNewSession(authInput, userInfo, restClient);
  }

  @Test
  void loginByCredentialsCreatesSessionOnlyAfterFetchingAndValidatingSiteInfo() {
    SessionPort sessionPort = mock(SessionPort.class);
    stubAuthenticationContext(sessionPort);
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
    when(sessionPort.generateNewSession(authInput, userInfo, restClient)).thenReturn(sessionToken);

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
            new AsyncOperations(Runnable::run),
            new CurrentSessionContext());

    SessionToken result = authService.loginByCredentials(authInput);

    assertEquals("credential-token", result.getToken());
    InOrder inOrder = inOrder(siteInfoApiPort, sessionPort);
    inOrder.verify(siteInfoApiPort).fetchSiteInfo();
    inOrder.verify(sessionPort).generateNewSession(authInput, userInfo, restClient);
    verify(searchUserByUserNameUseCase).searchUserByUserName("teacher");
    verify(syncUserInfoUseCase).syncActualUser(userInfo);
    verify(siteInfoPersistencePort).save(siteInfo);
  }

  @Test
  void loginByCredentialsFetchesSiteInfoAndSearchesUserConcurrently() {
    SessionPort sessionPort = mock(SessionPort.class);
    stubAuthenticationContext(sessionPort);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    TenantDatabasePort tenantDatabasePort = mock(TenantDatabasePort.class);
    SiteInfoApiPort siteInfoApiPort = mock(SiteInfoApiPort.class);
    SearchUserByUserNameUseCase searchUserByUserNameUseCase =
        mock(SearchUserByUserNameUseCase.class);
    SyncUserInfoUseCase syncUserInfoUseCase = mock(SyncUserInfoUseCase.class);
    CountDownLatch concurrentCallsStarted = new CountDownLatch(2);
    ExecutorService executor = Executors.newFixedThreadPool(2);
    CurrentSessionContext currentSessionContext = new CurrentSessionContext();
    Executor contextAwareExecutor =
        command -> {
          SessionData capturedSessionData = currentSessionContext.getSessionData();
          executor.execute(
              () -> currentSessionContext.runWithSessionData(capturedSessionData, command));
        };
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
    SessionData expectedSessionData =
        SessionData.builder()
            .host(authInput.getHost())
            .username(authInput.getUsername())
            .dbPassword(authInput.getDbPassword())
            .moodleToken("moodle-token")
            .restClient(restClient)
            .build();

    when(moodleApiPort.login(authInput)).thenReturn("moodle-token");
    when(moodleApiPort.loginWeb("teacher", "moodle-password", authInput.getHost()))
        .thenReturn(restClient);
    doAnswer(
            invocation -> {
              assertEquals(expectedSessionData, currentSessionContext.getSessionData());
              assertTenantSessionDataValue(
                  invocation.getArgument(0, SessionData.class), authInput, "teacher");
              return true;
            })
        .when(tenantDatabasePort)
        .initializeTenant(any());
    when(siteInfoApiPort.fetchSiteInfo())
        .thenAnswer(
            invocation -> {
              assertEquals(expectedSessionData, currentSessionContext.getSessionData());
              awaitConcurrentCall(concurrentCallsStarted);
              return siteInfo;
            });
    when(searchUserByUserNameUseCase.searchUserByUserName("teacher"))
        .thenAnswer(
            invocation -> {
              assertEquals(expectedSessionData, currentSessionContext.getSessionData());
              awaitConcurrentCall(concurrentCallsStarted);
              return userInfo;
            });
    when(syncUserInfoUseCase.syncActualUser(userInfo)).thenReturn(userInfo);
    when(sessionPort.generateNewSession(authInput, userInfo, restClient)).thenReturn(sessionToken);

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
            new AsyncOperations(contextAwareExecutor),
            currentSessionContext);

    try {
      SessionToken result = authService.loginByCredentials(authInput);
      assertEquals("credential-token", result.getToken());
      verify(tenantDatabasePort).initializeTenant(any());
      assertTenantSessionData(tenantDatabasePort, authInput, "teacher");
      verify(searchUserByUserNameUseCase).searchUserByUserName("teacher");
      verify(syncUserInfoUseCase).syncActualUser(userInfo);
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

  private static void assertCredentialsSiteInfoFailure(SiteInfo siteInfo) {
    SessionPort sessionPort = mock(SessionPort.class);
    stubAuthenticationContext(sessionPort);
    MoodleApiPort moodleApiPort = mock(MoodleApiPort.class);
    TenantDatabasePort tenantDatabasePort = mock(TenantDatabasePort.class);
    SiteInfoApiPort siteInfoApiPort = mock(SiteInfoApiPort.class);
    SearchUserByUserNameUseCase searchUserByUserNameUseCase =
        mock(SearchUserByUserNameUseCase.class);
    CurrentSessionContext currentSessionContext = new CurrentSessionContext();
    AtomicInteger scopedTaskCount = new AtomicInteger();
    Executor controlledExecutor =
        command -> {
          if (currentSessionContext.getSessionData() == null
              || scopedTaskCount.getAndIncrement() == 0) {
            command.run();
          }
        };
    TrackingAsyncOperations asyncOperations =
        new TrackingAsyncOperations(controlledExecutor, currentSessionContext);
    AuthInput authInput =
        new AuthInput(
            "teacher",
            "moodle-password",
            URI.create("https://moodle.test"),
            "database-password",
            null,
            null);
    RestClient restClient = mock(RestClient.class);

    when(moodleApiPort.login(authInput)).thenReturn("moodle-token");
    when(moodleApiPort.loginWeb("teacher", "moodle-password", authInput.getHost()))
        .thenReturn(restClient);
    when(siteInfoApiPort.fetchSiteInfo()).thenReturn(siteInfo);

    AuthService authService =
        new AuthService(
            searchUserByUserNameUseCase,
            mock(SyncUserInfoUseCase.class),
            mock(GetUserInfoPersistenceAdapterPort.class),
            siteInfoApiPort,
            mock(SiteInfoPersistencePort.class),
            sessionPort,
            moodleApiPort,
            tenantDatabasePort,
            asyncOperations,
            currentSessionContext);

    assertThrows(UnauthorizedException.class, () -> authService.loginByCredentials(authInput));

    assertTenantSessionData(tenantDatabasePort, authInput, "teacher");
    assertEquals(2, asyncOperations.scopedFutures().size());
    assertTrue(asyncOperations.scopedFutures().get(1).isCancelled());
    verify(searchUserByUserNameUseCase, never()).searchUserByUserName(any());
    verify(sessionPort, never()).generateNewSession(any(), any(), any());
  }

  private static void assertTenantSessionData(
      TenantDatabasePort tenantDatabasePort, AuthInput authInput, String username) {
    ArgumentCaptor<SessionData> sessionDataCaptor = ArgumentCaptor.forClass(SessionData.class);
    verify(tenantDatabasePort).initializeTenant(sessionDataCaptor.capture());
    assertTenantSessionDataValue(sessionDataCaptor.getValue(), authInput, username);
  }

  private static void assertTenantSessionDataValue(
      SessionData sessionData, AuthInput authInput, String username) {
    assertEquals(authInput.getHost(), sessionData.getHost());
    assertEquals(username, sessionData.getUsername());
    assertEquals(authInput.getDbPassword(), sessionData.getDbPassword());
  }

  private static final class TrackingAsyncOperations extends AsyncOperations {

    private final CurrentSessionContext currentSessionContext;
    private final List<CompletableFuture<?>> scopedFutures = new ArrayList<>();

    private TrackingAsyncOperations(
        Executor executor, CurrentSessionContext currentSessionContext) {
      super(executor);
      this.currentSessionContext = currentSessionContext;
    }

    @Override
    public <T> CompletableFuture<T> supplyAsync(Supplier<T> task) {
      CompletableFuture<T> future = super.supplyAsync(task);
      if (currentSessionContext.getSessionData() != null) {
        scopedFutures.add(future);
      }
      return future;
    }

    private List<CompletableFuture<?>> scopedFutures() {
      return scopedFutures;
    }
  }

  @Test
  void loginByCredentialsDoesNotInvalidateAnExistingSessionWhenLaterProcessingFails() {
    SessionPort sessionPort = mock(SessionPort.class);
    stubAuthenticationContext(sessionPort);
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
            new AsyncOperations(Runnable::run),
            new CurrentSessionContext());

    assertThrows(IllegalStateException.class, () -> authService.loginByCredentials(authInput));

    verify(searchUserByUserNameUseCase).searchUserByUserName("teacher");
    verify(syncUserInfoUseCase).syncActualUser(userInfo);
    verify(sessionPort, never()).invalidateSession(existingSessionToken);
  }

  private static void stubAuthenticationContext(SessionPort sessionPort) {
    when(sessionPort.buildAuthenticationContext(any(), nullable(RestClient.class)))
        .thenAnswer(
            invocation -> {
              AuthInput input = invocation.getArgument(0);
              RestClient restClient = invocation.getArgument(1);
              return SessionData.builder()
                  .host(input.getHost())
                  .username(input.getUsername())
                  .dbPassword(input.getDbPassword())
                  .moodleToken(input.getMoodleToken())
                  .restClient(restClient)
                  .build();
            });
  }
}
