package es.ubu.lsi.ubumonitoranalytics.features.auth.infrastructure.out.moodle;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.core.webservice.getsiteinfo.request.GetSiteInfoRequestApi;
import es.ubu.lsi.moodle.model.core.webservice.getsiteinfo.response.GetSiteInfoResponseApi;
import es.ubu.lsi.moodle.model.login.token.request.LoginTokenRequestApi;
import es.ubu.lsi.moodle.model.login.token.response.LoginTokenResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out.MoodleApiPort;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.Cookie;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.jsoup.Jsoup;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

/** Implements authentication and web-client operations against Moodle. */
@Component
@RequiredArgsConstructor
@Slf4j
public class MoodleClientAdapter implements MoodleApiPort {

  private final Client client;
  private final MoodleAuthMapper mapper;
  private final RestClient.Builder builder;
  private final Logbook logbook;

  @Override
  public String login(AuthInput authInput) {
    LoginTokenRequestApi loginTokenRequest = mapper.toDto(authInput);
    log.trace(
        "Mapped Moodle login request; hostPresent={}, usernamePresent={}, passwordPresent={}",
        loginTokenRequest.getBaseurl() != null,
        loginTokenRequest.getUsername() != null,
        loginTokenRequest.getPassword() != null);
    LoginTokenResponseApi loginTokenResponse = client.login(loginTokenRequest);

    log.trace(
        "Moodle login response mapped; tokenPresent={}, errorPresent={}",
        loginTokenResponse.getToken() != null,
        loginTokenResponse.getError() != null);
    return loginTokenResponse.getToken();
  }

  @Override
  public String usernameByToken(AuthInput authInput) {
    try {
      GetSiteInfoResponseApi siteInfoResponse =
          client.coreWebservice().getSiteInfo(new GetSiteInfoRequestApi());
      return siteInfoResponse.getUsername();
    } catch (Exception e) {
      log.info("Error fetching username by token: {}", e.getMessage());
      return null;
    }
  }

  @Override
  public RestClient getRestClientFromCookies(AuthInput authInput) {
    String cookieHeader = buildCookieHeader(authInput.getCookies());

    CloseableHttpClient httpClient =
        HttpClients.custom().build(); // No cookie store is needed here.
    HttpComponentsClientHttpRequestFactory requestFactory =
        new HttpComponentsClientHttpRequestFactory(httpClient);

    ClientHttpRequestFactory bufferingFactory =
        new BufferingClientHttpRequestFactory(requestFactory);

    return builder
        .baseUrl(authInput.getHost())
        .requestFactory(bufferingFactory)
        .defaultHeader(HttpHeaders.COOKIE, cookieHeader)
        .requestInterceptor(new LogbookClientHttpRequestInterceptor(logbook))
        .build();
  }

  private static String buildCookieHeader(List<Cookie> cookies) {
    if (cookies == null || cookies.isEmpty()) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < cookies.size(); i++) {
      Cookie cookie = cookies.get(i);
      if (i > 0) {
        sb.append("; ");
      }
      sb.append(cookie.getName()).append('=').append(cookie.getValue());
    }
    return sb.toString();
  }

  @Override
  public RestClient loginWeb(String username, String password, URI host) {
    CookieStore cookieStore = new BasicCookieStore();

    CloseableHttpClient httpClient =
        HttpClients.custom().setDefaultCookieStore(cookieStore).build();

    RestClient client =
        builder
            .baseUrl(host)
            .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
            .build();

    String loginToken = getLoginToken(client);
    // 2. login
    postLogin(username, password, loginToken, client);

    return client;
  }

  private static void postLogin(
      String username, String password, String loginToken, RestClient client) {

    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("username", username);
    form.add("password", password);
    form.add("logintoken", loginToken);

    client
        .post()
        .uri("/login/index.php")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(form)
        .retrieve()
        .toBodilessEntity();
  }

  private String getLoginToken(RestClient client) {

    String html = client.get().uri("/login/index.php").retrieve().body(String.class);

    assert html != null;
    return Jsoup.parse(html).select("input[name=logintoken]").attr("value");
  }
}
