package es.ubu.lsi.moodle.http;

import es.ubu.lsi.moodle.core.HttpTransport;
import es.ubu.lsi.moodle.json.JacksonMapper;
import es.ubu.lsi.moodle.model.ajax.AjaxResponse;
import es.ubu.lsi.moodle.model.login.token.response.LoginTokenResponseApi;
import es.ubu.lsi.moodle.utils.PhpQueryParamBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/** Sends Moodle requests with the JDK HTTP client and maps JSON responses. */
@RequiredArgsConstructor
public class JavaHttpTransport implements HttpTransport {

  private final HttpClient client = HttpClient.newHttpClient();
  private final JacksonMapper mapper;
  private final URI baseUrl;
  private final String token;

  /** Sends a Moodle login request and maps the token response. */
  @Override
  public LoginTokenResponseApi login(URI baseUrl, String context, Map<String, String> form)
      throws Exception {
    String body = execute(context, form);
    return mapper.fromJson(body, LoginTokenResponseApi.class);
  }

  /** Sends form data and maps one response object. */
  @Override
  public <T> T postForm(String context, Map<String, String> form, Class<T> responseType)
      throws Exception {
    String body = execute(context, form);
    return mapper.fromJson(body, responseType);
  }

  /** Sends form data and maps an array response. */
  @Override
  public <T> List<T> postFormArray(String context, Map<String, String> form, Class<T> elementType)
      throws Exception {
    String body = execute(context, form);
    return mapper.fromJsonArray(body, elementType);
  }

  /** Sends JSON and maps one response object. */
  @Override
  public <T> T postJson(String context, Object body, Class<T> responseType) throws Exception {
    String responseBody = executeJson(context, body);
    return mapper.fromJson(responseBody, responseType);
  }

  /** Sends JSON and maps an array response. */
  @Override
  public <T> List<T> postJsonArray(String context, Object body, Class<T> elementType)
      throws Exception {
    String responseBody = executeJson(context, body);
    return mapper.fromJsonArray(responseBody, elementType);
  }

  /** Sends JSON and maps Moodle AJAX responses. */
  @Override
  public <T> List<AjaxResponse<T>> postJsonAjaxArray(String context, Object body, Class<T> dataType)
      throws Exception {
    String responseBody = executeJson(context, body);
    return mapper.fromJsonAjaxArray(responseBody, dataType);
  }

  private String executeJson(String context, Object body) throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(baseUrl.resolve(context))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(mapper.toJson(body)))
            .build();

    return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
  }

  /** Returns the token used by this transport. */
  @Override
  public String getToken() {
    return token;
  }

  private String execute(String context, Map<String, String> form)
      throws IOException, InterruptedException {
    String urlParams = PhpQueryParamBuilder.ofFormData(form);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(baseUrl.resolve(context))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(urlParams))
            .build();

    return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
  }
}
