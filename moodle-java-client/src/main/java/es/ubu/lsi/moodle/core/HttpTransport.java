package es.ubu.lsi.moodle.core;

import es.ubu.lsi.moodle.model.ajax.AjaxResponse;
import es.ubu.lsi.moodle.model.login.token.response.LoginTokenResponseApi;
import java.net.URI;
import java.util.List;
import java.util.Map;

/** Sends HTTP requests to Moodle and exposes the configured authentication token. */
public interface HttpTransport {

  /**
   * Requests a Moodle token using form data.
   *
   * @param baseUrl Moodle site base URI
   * @param context relative endpoint path
   * @param form form parameters
   * @return token response
   * @throws Exception if the request or response mapping fails
   */
  LoginTokenResponseApi login(URI baseUrl, String context, Map<String, String> form)
      throws Exception;

  /**
   * Posts form data and maps the response.
   *
   * @param context relative endpoint path
   * @param form form parameters
   * @param responseType response model class
   * @param <T> response model type
   * @return mapped response
   * @throws Exception if the request or response mapping fails
   */
  <T> T postForm(String context, Map<String, String> form, Class<T> responseType) throws Exception;

  /**
   * Posts form data and maps a response array.
   *
   * @param context relative endpoint path
   * @param form form parameters
   * @param elementType response element class
   * @param <T> response element type
   * @return mapped response elements
   * @throws Exception if the request or response mapping fails
   */
  <T> List<T> postFormArray(String context, Map<String, String> form, Class<T> elementType)
      throws Exception;

  /**
   * Posts JSON and maps the response.
   *
   * @param context relative endpoint path
   * @param body request body
   * @param responseType response model class
   * @param <T> response model type
   * @return mapped response
   * @throws Exception if the request or response mapping fails
   */
  <T> T postJson(String context, Object body, Class<T> responseType) throws Exception;

  /**
   * Posts JSON and maps a response array.
   *
   * @param context relative endpoint path
   * @param body request body
   * @param elementType response element class
   * @param <T> response element type
   * @return mapped response elements
   * @throws Exception if the request or response mapping fails
   */
  <T> List<T> postJsonArray(String context, Object body, Class<T> elementType) throws Exception;

  /**
   * Posts JSON and maps an AJAX response array.
   *
   * @param context relative endpoint path
   * @param body AJAX request body
   * @param dataType response data class
   * @param <T> response data type
   * @return mapped AJAX responses
   * @throws Exception if the request or response mapping fails
   */
  <T> List<AjaxResponse<T>> postJsonAjaxArray(String context, Object body, Class<T> dataType)
      throws Exception;

  /**
   * @return configured Moodle web-service token
   */
  String getToken();

  /**
   * @return Moodle web-service response format
   */
  default String wsFormat() {
    return "json";
  }
}
