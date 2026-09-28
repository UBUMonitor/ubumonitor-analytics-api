package es.ubu.lsi.moodle.core;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.api.core.calendar.CoreCalendarApi;
import es.ubu.lsi.moodle.api.core.calendar.CoreCalendarApiImpl;
import es.ubu.lsi.moodle.api.core.completion.CoreCompletionApi;
import es.ubu.lsi.moodle.api.core.completion.CoreCompletionApiImpl;
import es.ubu.lsi.moodle.api.core.course.CoreCourseApi;
import es.ubu.lsi.moodle.api.core.course.CoreCourseApiImpl;
import es.ubu.lsi.moodle.api.core.enrol.CoreEnrolApi;
import es.ubu.lsi.moodle.api.core.enrol.CoreEnrolApiImpl;
import es.ubu.lsi.moodle.api.core.user.CoreUserApi;
import es.ubu.lsi.moodle.api.core.user.CoreUserApiImpl;
import es.ubu.lsi.moodle.api.core.webservice.CoreWebserviceApi;
import es.ubu.lsi.moodle.api.core.webservice.CoreWebserviceApiImpl;
import es.ubu.lsi.moodle.api.gradereport.GradeReportUserApi;
import es.ubu.lsi.moodle.api.gradereport.GradeReportUserApiImpl;
import es.ubu.lsi.moodle.api.mod.forum.ModForumApi;
import es.ubu.lsi.moodle.api.mod.forum.ModForumApiImpl;
import es.ubu.lsi.moodle.api.tool.mobile.ToolMobileApi;
import es.ubu.lsi.moodle.api.tool.mobile.ToolMobileApiImpl;
import es.ubu.lsi.moodle.exception.MoodleApiException;
import es.ubu.lsi.moodle.exception.MoodleUnauthorizedException;
import es.ubu.lsi.moodle.model.ajax.AjaxRequest;
import es.ubu.lsi.moodle.model.ajax.AjaxResponse;
import es.ubu.lsi.moodle.model.login.token.request.LoginTokenRequestApi;
import es.ubu.lsi.moodle.model.login.token.response.LoginTokenResponseApi;
import es.ubu.lsi.moodle.utils.PhpQueryParamBuilder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DefaultClient implements Client {
  private static final String LOGIN_ENDPOINT = "/login/token.php";
  private static final String SERVER_ENDPOINT = "/webservice/rest/server.php";

  private final HttpTransport transport;

  private final CoreCalendarApi coreCalendar;
  private final CoreCompletionApi coreCompletion;
  private final CoreCourseApi coreCourse;
  private final CoreEnrolApi coreEnrol;
  private final CoreUserApi coreUser;
  private final CoreWebserviceApi coreWebservice;

  private final GradeReportUserApi gradeReportUser;
  private final ModForumApi modForum;
  private final ToolMobileApi toolMobile;

  public DefaultClient(HttpTransport httpTransport) {
    this.transport = httpTransport;
    this.coreCalendar = new CoreCalendarApiImpl(this);
    this.coreUser = new CoreUserApiImpl(this);
    this.coreCourse = new CoreCourseApiImpl(this);
    this.coreEnrol = new CoreEnrolApiImpl(this);
    this.coreCompletion = new CoreCompletionApiImpl(this);
    this.coreWebservice = new CoreWebserviceApiImpl(this);
    this.gradeReportUser = new GradeReportUserApiImpl(this);
    this.modForum = new ModForumApiImpl(this);
    this.toolMobile = new ToolMobileApiImpl(this);
  }

  @Override
  public CoreUserApi coreUser() {
    return coreUser;
  }

  @Override
  public CoreCourseApi coreCourse() {
    return coreCourse;
  }

  @Override
  public CoreEnrolApi coreEnrol() {
    return coreEnrol;
  }

  @Override
  public CoreCalendarApi coreCalendar() {
    return coreCalendar;
  }

  @Override
  public CoreCompletionApi coreCompletion() {
    return coreCompletion;
  }

  @Override
  public CoreWebserviceApi coreWebservice() {
    return coreWebservice;
  }

  @Override
  public GradeReportUserApi gradeReportUser() {
    return gradeReportUser;
  }

  @Override
  public ModForumApi modForum() {
    return modForum;
  }

  @Override
  public ToolMobileApi toolMobile() {
    return toolMobile;
  }

  @Override
  public LoginTokenResponseApi login(LoginTokenRequestApi request) {
    try {
      Map<String, String> form = PhpQueryParamBuilder.toPhpQuery(request);
      LoginTokenResponseApi loginTokenResponseApi =
          transport.login(request.getBaseurl(), LOGIN_ENDPOINT, form);
      if (loginTokenResponseApi.getError() != null) {
        throw new MoodleUnauthorizedException(loginTokenResponseApi.getError());
      }
      return loginTokenResponseApi;

    } catch (MoodleUnauthorizedException e) {
      throw e;
    } catch (Exception e) {
      throw new MoodleUnauthorizedException("Login failed", e);
    }
  }

  @Override
  public <T> T execute(Object request, Class<T> responseType) {
    try {
      Map<String, String> form = getForm(request);
      return transport.postForm(SERVER_ENDPOINT, form, responseType);

    } catch (Exception e) {
      throw new MoodleApiException(e);
    }
  }

  @Override
  public <T> List<T> executeList(Object request, Class<T> elementType) {
    try {
      Map<String, String> form = getForm(request);
      return transport.postFormArray(SERVER_ENDPOINT, form, elementType);

    } catch (Exception e) {
      throw new MoodleApiException(e);
    }
  }

  @Override
  public <T> AjaxResponse<T> executeAjax(
      String context, List<? extends AjaxRequest<?>> requests, Class<T> responseType) {
    List<AjaxResponse<T>> responses;
    try {
      responses = transport.postJsonAjaxArray(context, requests, responseType);
    } catch (Exception e) {
      throw new MoodleApiException(e);
    }
    if (responses.size() != 1) {
      throw new MoodleApiException("Expected one Ajax response but received " + responses.size());
    }
    return responses.getFirst();
  }

  @Override
  public <T> List<AjaxResponse<T>> executeAjaxList(
      String context, List<? extends AjaxRequest<?>> requests, Class<T> elementType) {
    try {
      return transport.postJsonAjaxArray(context, requests, elementType);
    } catch (Exception e) {
      throw new MoodleApiException(e);
    }
  }

  private Map<String, String> getForm(Object request) {
    Map<String, String> requestParameters = PhpQueryParamBuilder.toPhpQuery(request);
    Map<String, String> form = new LinkedHashMap<>();
    requestParameters.forEach(
        (key, value) -> {
          if (key.startsWith("args[")) {
            int argsEnd = key.indexOf(']', "args[".length());
            if (argsEnd < 0) {
              throw new IllegalArgumentException("Malformed args query parameter: " + key);
            }
            form.put(key.substring("args[".length(), argsEnd) + key.substring(argsEnd + 1), value);
          } else {
            form.put(key, value);
          }
        });
    form.put("moodlewsrestformat", transport.wsFormat());
    form.put("wstoken", transport.getToken());
    return form;
  }
}
