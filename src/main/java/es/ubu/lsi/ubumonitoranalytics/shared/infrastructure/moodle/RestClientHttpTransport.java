package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle;

import es.ubu.lsi.moodle.core.HttpTransport;
import es.ubu.lsi.moodle.model.login.token.response.LoginTokenResponseApi;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class RestClientHttpTransport implements HttpTransport {

  @Qualifier("moodleRestClient")
  private final RestClient restClient;

  private final CurrentSessionContext currentSessionContext;

  @Override
  public LoginTokenResponseApi login(URI baseUrl, String context, Map<String, String> form)
      throws Exception {
    URI finalUri = getUri(baseUrl, context);

    return restClient
        .post()
        .uri(finalUri)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(toFormData(form))
        .retrieve()
        .body(LoginTokenResponseApi.class);
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
  public String getToken() {
    SessionData sessionData = currentSessionContext.getSessionData();
    return sessionData.getMoodleToken();
  }

  private MultiValueMap<String, String> toFormData(Map<String, String> form) {

    LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.setAll(form);
    return map;
  }

  private static URI getUri(URI host, String context) {
    return UriComponentsBuilder.fromUri(host).path(context).build().toUri();
  }
}
