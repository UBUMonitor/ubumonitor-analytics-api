package es.ubu.lsi.moodle.core;

import es.ubu.lsi.moodle.model.ajax.AjaxResponse;
import es.ubu.lsi.moodle.model.login.token.response.LoginTokenResponseApi;
import java.net.URI;
import java.util.List;
import java.util.Map;

public interface HttpTransport {

  LoginTokenResponseApi login(URI baseUrl, String context, Map<String, String> form)
      throws Exception;

  <T> T postForm(String context, Map<String, String> form, Class<T> responseType) throws Exception;

  <T> List<T> postFormArray(String context, Map<String, String> form, Class<T> elementType)
      throws Exception;

  <T> T postJson(String context, Object body, Class<T> responseType) throws Exception;

  <T> List<T> postJsonArray(String context, Object body, Class<T> elementType) throws Exception;

  <T> List<AjaxResponse<T>> postJsonAjaxArray(String context, Object body, Class<T> dataType)
      throws Exception;

  String getToken();

  default String wsFormat() {
    return "json";
  }
}
