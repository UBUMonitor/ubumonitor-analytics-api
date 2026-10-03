package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle;

import es.ubu.lsi.moodle.core.HttpTransport;
import es.ubu.lsi.moodle.model.ajax.AjaxResponse;
import es.ubu.lsi.moodle.model.login.token.response.LoginTokenResponseApi;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/** Adapts Spring's RestClient to the Moodle client's transport contract. */
@Component
@RequiredArgsConstructor
@Slf4j
public class RestClientHttpTransport implements HttpTransport {

  @Qualifier("moodleRestClient")
  private final RestClient restClient;

  private final CurrentSessionContext currentSessionContext;

  @Override
  public LoginTokenResponseApi login(URI baseUrl, String context, Map<String, String> form)
      throws Exception {
    URI finalUri = getUri(baseUrl, context);
    MultiValueMap<String, String> formData = toFormData(form);
    log.trace(
        "Sending Moodle login form to {}; parameter count={}, keys={}",
        finalUri.getPath(),
        formData.size(),
        formData.keySet());

    try {
      LoginTokenResponseApi response =
          restClient
              .post()
              .uri(finalUri)
              .contentType(MediaType.APPLICATION_FORM_URLENCODED)
              .body(formData)
              .retrieve()
              .body(LoginTokenResponseApi.class);
      log.trace(
          "Moodle login response deserialized; responsePresent={}, tokenPresent={}, errorPresent={}",
          response != null,
          response != null && response.getToken() != null,
          response != null && response.getError() != null);
      return response;
    } catch (RuntimeException exception) {
      log.trace(
          "Moodle login request or response mapping failed for {}; form keys={}",
          finalUri.getPath(),
          formData.keySet(),
          exception);
      throw exception;
    }
  }

  @Override
  public <T> T postForm(String context, Map<String, String> form, Class<T> clazz) {
    SessionData sessionData = currentSessionContext.getSessionData();

    URI finalUri = getUri(sessionData.getHost(), context);

    return restClient
        .post()
        .uri(finalUri)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(toFormData(form))
        .retrieve()
        .body(clazz);
  }

  @Override
  public <T> List<T> postFormArray(String context, Map<String, String> form, Class<T> elementType) {

    ParameterizedTypeReference<List<T>> typeRef =
        ParameterizedTypeReference.forType(
            ResolvableType.forClassWithGenerics(List.class, elementType).getType());

    SessionData sessionData = currentSessionContext.getSessionData();
    URI finalUri = getUri(sessionData.getHost(), context);

    return restClient
        .post()
        .uri(finalUri)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(toFormData(form))
        .retrieve()
        .body(typeRef);
  }

  @Override
  public <T> T postJson(String context, Object body, Class<T> responseType) {
    SessionData sessionData = currentSessionContext.getSessionData();
    URI finalUri = getUri(sessionData.getHost(), context);

    return restClient
        .post()
        .uri(finalUri)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .retrieve()
        .body(responseType);
  }

  @Override
  public <T> List<T> postJsonArray(String context, Object body, Class<T> elementType) {
    ParameterizedTypeReference<List<T>> typeRef =
        ParameterizedTypeReference.forType(
            ResolvableType.forClassWithGenerics(List.class, elementType).getType());

    SessionData sessionData = currentSessionContext.getSessionData();
    URI finalUri = getUri(sessionData.getHost(), context);

    return restClient
        .post()
        .uri(finalUri)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .retrieve()
        .body(typeRef);
  }

  @Override
  public <T> List<AjaxResponse<T>> postJsonAjaxArray(
      String context, Object body, Class<T> dataType) {
    ResolvableType responseType = ResolvableType.forClassWithGenerics(AjaxResponse.class, dataType);
    ParameterizedTypeReference<List<AjaxResponse<T>>> typeRef =
        ParameterizedTypeReference.forType(
            ResolvableType.forClassWithGenerics(List.class, responseType).getType());

    SessionData sessionData = currentSessionContext.getSessionData();
    URI finalUri = getUri(sessionData.getHost(), context);

    return restClient
        .post()
        .uri(finalUri)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .retrieve()
        .body(typeRef);
  }

  @Override
  public String getToken() {
    SessionData sessionData = currentSessionContext.getSessionData();
    return sessionData.getMoodleToken();
  }

  private MultiValueMap<String, String> toFormData(Map<String, String> form) {

    LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.setAll(form);
    log.trace(
        "Converted Moodle form map to MultiValueMap; source count={}, output count={}, keys={}",
        form.size(),
        map.size(),
        map.keySet());
    return map;
  }

  private static URI getUri(URI host, String context) {
    return UriComponentsBuilder.fromUri(host).path(context).build().toUri();
  }
}
