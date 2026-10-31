package es.ubu.lsi.ubumonitoranalytics.features.auth.application.service;

import es.ubu.lsi.moodle.exception.MoodleUnauthorizedException;
import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.in.AuthUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out.MoodleApiPort;
import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out.SessionPort;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.SessionToken;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.SiteInfoApiPort;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.SiteInfoPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.in.SyncUserInfoUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.out.GetUserInfoPersistenceAdapterPort;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.UnauthorizedException;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.database.TenantDatabasePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.concurrent.AsyncOperations;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/** Coordinates Moodle authentication and application session creation. */
@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

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
    SessionToken sessionToken = sessionPort.generateSession(authInput, null, null);
    try {
      SiteInfo siteInfo = siteInfoApiPort.fetchSiteInfo();
      if (siteInfo == null || siteInfo.getSite() == null) {
        throw new UnauthorizedException("Invalid token.");
      }
      String userName = siteInfo.getSite().getUserName();
      if (userName == null || userName.isBlank()) {
        throw new UnauthorizedException("Invalid token.");
      }
      authInput.setUsername(userName);
      initializeTenantDatabase(authInput);
      RestClient restClient = moodleApiPort.getRestClientFromCookies(authInput);
      sessionToken = sessionPort.updateSession(authInput, sessionToken, null, restClient);
      siteInfoPersistencePort.save(siteInfo);
      UserInfo userInfo = syncUserInfoUseCase.syncActualUser(userName);
      return sessionPort.updateSession(authInput, sessionToken, userInfo, restClient);
    } catch (RuntimeException exception) {
      sessionPort.invalidateSession(sessionToken);
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
    SessionToken sessionToken = sessionPort.generateSession(authInput, null, restClient);
    try {
      SiteInfo siteInfo = siteInfoApiPort.fetchSiteInfo();
      siteInfoPersistencePort.save(siteInfo);
      UserInfo userInfo = syncUserInfoUseCase.syncActualUser(authInput.getUsername());
      return sessionPort.updateSession(authInput, sessionToken, userInfo, restClient);
    } catch (RuntimeException exception) {
      sessionPort.invalidateSession(sessionToken);
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
    SessionToken sessionToken = sessionPort.generateSession(authInput, null, null);
    try {
      UserInfo userInfo = getUserInfoPersistenceAdapterPort.getActualUser(authInput.getUsername());
      if (userInfo == null) {
        throw new UnauthorizedException("Invalid username");
      }
      return sessionPort.updateSession(authInput, sessionToken, userInfo, null);
    } catch (RuntimeException exception) {
      sessionPort.invalidateSession(sessionToken);
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

  private RestClient fetchRestClient(AuthInput authInput) {
    return moodleApiPort.loginWeb(
        authInput.getUsername(), authInput.getPassword(), authInput.getHost());
  }
}
