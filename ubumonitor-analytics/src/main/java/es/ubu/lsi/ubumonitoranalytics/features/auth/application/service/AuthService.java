package es.ubu.lsi.ubumonitoranalytics.features.auth.application.service;

import es.ubu.lsi.moodle.exception.MoodleUnauthorizedException;
import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.in.AuthUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out.MoodleApiPort;
import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out.SessionPort;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.AuthCurrentUser;
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
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.concurrent.AsyncOperations;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/** Coordinates Moodle authentication and application session creation. */
@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

  private final SearchUserByUserNameUseCase searchUserByUserNameUseCase;
  private final SyncUserInfoUseCase syncUserInfoUseCase;
  private final GetUserInfoPersistenceAdapterPort getUserInfoPersistenceAdapterPort;
  private final SiteInfoApiPort siteInfoApiPort;
  private final SiteInfoPersistencePort siteInfoPersistencePort;
  private final SessionPort sessionPort;
  private final MoodleApiPort moodleApiPort;
  private final TenantDatabasePort tenantDatabasePort;
  private final AsyncOperations asyncOperations;

  /**
   * Authenticates an SSO launch request and associates a web client with the session.
   *
   * @param authInput Moodle token, site, and optional cookies
   * @return the application session token
   * @throws UnauthorizedException when Moodle does not resolve the token to a username
   */
  @Override
  public SessionToken loginBySSO(AuthInput authInput) {
    SessionToken sessionToken = null;
    try {
      sessionPort.setAuthenticationContext(authInput, null);
      SiteInfo siteInfo = siteInfoApiPort.fetchSiteInfo();
      if (siteInfo == null || siteInfo.getSite() == null) {
        throw new UnauthorizedException("Invalid token.");
      }
      String userName = siteInfo.getSite().getUserName();
      if (userName == null || userName.isBlank()) {
        throw new UnauthorizedException("Invalid token.");
      }
      authInput.setUsername(userName);
      sessionPort.setAuthenticationContext(authInput, null);
      initializeTenantDatabase(authInput);
      siteInfoPersistencePort.save(siteInfo);

      RestClient restClient = moodleApiPort.getRestClientFromCookies(authInput);
      sessionPort.setAuthenticationContext(authInput, restClient);
      UserInfo userInfo =
          syncUserInfoUseCase.syncActualUser(
              searchUserByUserNameUseCase.searchUserByUserName(userName));
      validateSiteUser(siteInfo);
      sessionToken = sessionPort.generateNewSession(authInput, userInfo, restClient);
      return attachSiteUserInfo(sessionToken, siteInfo, userInfo);
    } catch (RuntimeException exception) {
      if (sessionToken != null) {
        sessionPort.invalidateSession(sessionToken);
      }
      throw exception;
    }
  }

  /**
   * Authenticates with Moodle credentials and creates an application session.
   *
   * @param authInput Moodle username, password, and site
   * @return the application session token
   * @throws UnauthorizedException when Moodle rejects the credentials
   */
  @Override
  public SessionToken loginByCredentials(AuthInput authInput) {

    CompletableFuture<String> moodleTokenFuture =
        asyncOperations.supplyAsync(() -> fetchMoodleToken(authInput));
    CompletableFuture<RestClient> restClientFuture =
        asyncOperations.supplyAsync(() -> fetchRestClient(authInput));

    String moodleToken;
    try {
      moodleToken = moodleTokenFuture.join();
    } catch (CompletionException exception) {
      if (exception.getCause() instanceof MoodleUnauthorizedException) {
        throw new UnauthorizedException("Invalid Moodle credentials.");
      }
      throw exception;
    }
    if (moodleToken == null || moodleToken.isBlank()) {
      throw new UnauthorizedException("Invalid Moodle credentials.");
    }

    RestClient restClient = restClientFuture.join();

    authInput.setMoodleToken(moodleToken);
    initializeTenantDatabase(authInput);
    sessionPort.setAuthenticationContext(authInput, restClient);
    SessionToken sessionToken = null;
    CompletableFuture<UserInfo> searchedUserFuture = null;
    try {
      CompletableFuture<SiteInfo> siteInfoFuture =
          asyncOperations.supplyAsync(siteInfoApiPort::fetchSiteInfo);
      searchedUserFuture =
          asyncOperations.supplyAsync(
              () -> searchUserByUserNameUseCase.searchUserByUserName(authInput.getUsername()));
      SiteInfo siteInfo = joinCompletedFuture(siteInfoFuture);
      if (siteInfo == null || siteInfo.getSite() == null) {
        throw new UnauthorizedException("Invalid Moodle site information.");
      }
      if (!Objects.equals(authInput.getUsername(), siteInfo.getSite().getUserName())) {
        throw new UnauthorizedException("Invalid Moodle site information.");
      }
      validateSiteUser(siteInfo);
      siteInfoPersistencePort.save(siteInfo);

      UserInfo searchedUser = joinCompletedFuture(searchedUserFuture);
      UserInfo userInfo = syncUserInfoUseCase.syncActualUser(searchedUser);
      sessionToken = sessionPort.generateNewSession(authInput, userInfo, restClient);
      return attachSiteUserInfo(sessionToken, siteInfo, userInfo);
    } catch (RuntimeException exception) {
      if (searchedUserFuture != null) {
        searchedUserFuture.cancel(true);
      }
      if (sessionToken != null) {
        sessionPort.invalidateSession(sessionToken);
      }
      throw exception;
    }
  }

  /**
   * Creates an application session without an authenticated Moodle web client.
   *
   * @param authInput offline authentication details
   * @return the application session token
   */
  @Override
  public SessionToken loginOffline(AuthInput authInput) {

    initializeTenantDatabase(authInput);
    sessionPort.setAuthenticationContext(authInput, null);
    SessionToken sessionToken = null;
    try {
      UserInfo userInfo = getUserInfoPersistenceAdapterPort.getActualUser(authInput.getUsername());
      if (userInfo == null) {
        throw new UnauthorizedException("Invalid username");
      }
      sessionToken = sessionPort.generateNewSession(authInput, userInfo, null);
      return attachUserInfo(sessionToken, userInfo);
    } catch (RuntimeException exception) {
      if (sessionToken != null) {
        sessionPort.invalidateSession(sessionToken);
      }
      throw exception;
    }
  }

  private void initializeTenantDatabase(AuthInput authInput) {
    SessionData sessionData =
        SessionData.builder()
            .host(authInput.getHost())
            .username(authInput.getUsername())
            .dbPassword(authInput.getDbPassword())
            .build();
    tenantDatabasePort.initializeTenant(sessionData);
  }

  private String fetchMoodleToken(AuthInput authInput) {
    return moodleApiPort.login(authInput);
  }

  private <T> T joinCompletedFuture(CompletableFuture<T> future) {
    try {
      return future.join();
    } catch (CompletionException exception) {
      Throwable cause = exception.getCause();
      if (cause instanceof RuntimeException runtimeException) {
        throw runtimeException;
      }
      if (cause instanceof Error error) {
        throw error;
      }
      throw exception;
    }
  }

  private SessionToken attachSiteUserInfo(
      SessionToken sessionToken, SiteInfo siteInfo, UserInfo userInfo) {
    Site site = siteInfo.getSite();

    AuthCurrentUser currentUser = new AuthCurrentUser();
    currentUser.setId(site.getUserId());
    currentUser.setUserName(site.getUserName());
    currentUser.setFirstName(site.getFirstName());
    currentUser.setLastName(site.getLastName());
    currentUser.setFullName(site.getFullName());
    currentUser.setInitials(userInfo == null ? null : userInfo.getInitials());
    currentUser.setEmail(userInfo == null ? null : userInfo.getEmail());
    currentUser.setTimeZone(userInfo == null ? null : userInfo.getTimeZone());
    currentUser.setLang(userInfo == null ? null : userInfo.getLang());
    currentUser.setUserImageUrl(userInfo == null ? null : userInfo.getUserImageUrl());
    sessionToken.setCurrentUser(currentUser);
    return sessionToken;
  }

  private void validateSiteUser(SiteInfo siteInfo) {
    Site site = siteInfo.getSite();
    if (site.getUserId() == null || site.getUserName() == null || site.getUserName().isBlank()) {
      throw new UnauthorizedException("Invalid Moodle site information.");
    }
  }

  private SessionToken attachUserInfo(SessionToken sessionToken, UserInfo userInfo) {
    AuthCurrentUser currentUser = new AuthCurrentUser();
    currentUser.setId(userInfo.getId());
    currentUser.setUserName(userInfo.getUserName());
    currentUser.setFirstName(userInfo.getFirstName());
    currentUser.setLastName(userInfo.getLastName());
    currentUser.setFullName(userInfo.getFullName());
    currentUser.setInitials(userInfo.getInitials());
    currentUser.setEmail(userInfo.getEmail());
    currentUser.setTimeZone(userInfo.getTimeZone());
    currentUser.setLang(userInfo.getLang());
    currentUser.setUserImageUrl(userInfo.getUserImageUrl());
    sessionToken.setCurrentUser(currentUser);
    return sessionToken;
  }

  private RestClient fetchRestClient(AuthInput authInput) {
    return moodleApiPort.loginWeb(
        authInput.getUsername(), authInput.getPassword(), authInput.getHost());
  }
}
