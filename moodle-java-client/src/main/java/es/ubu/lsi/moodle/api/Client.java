package es.ubu.lsi.moodle.api;

import es.ubu.lsi.moodle.api.core.calendar.CoreCalendarApi;
import es.ubu.lsi.moodle.api.core.completion.CoreCompletionApi;
import es.ubu.lsi.moodle.api.core.course.CoreCourseApi;
import es.ubu.lsi.moodle.api.core.enrol.CoreEnrolApi;
import es.ubu.lsi.moodle.api.core.user.CoreUserApi;
import es.ubu.lsi.moodle.api.core.webservice.CoreWebserviceApi;
import es.ubu.lsi.moodle.api.gradereport.GradeReportUserApi;
import es.ubu.lsi.moodle.api.mod.forum.ModForumApi;
import es.ubu.lsi.moodle.api.tool.mobile.ToolMobileApi;
import es.ubu.lsi.moodle.model.ajax.AjaxRequest;
import es.ubu.lsi.moodle.model.ajax.AjaxResponse;
import es.ubu.lsi.moodle.model.login.token.request.LoginTokenRequestApi;
import es.ubu.lsi.moodle.model.login.token.response.LoginTokenResponseApi;
import java.util.List;

/** Entry point for Moodle web-service and AJAX operations. */
public interface Client {
  /**
   * @return the Moodle calendar API
   */
  CoreCalendarApi coreCalendar();

  /**
   * @return the Moodle user API
   */
  CoreUserApi coreUser();

  /**
   * @return the Moodle course API
   */
  CoreCourseApi coreCourse();

  /**
   * @return the Moodle enrollment API
   */
  CoreEnrolApi coreEnrol();

  /**
   * @return the Moodle activity completion API
   */
  CoreCompletionApi coreCompletion();

  /**
   * @return the Moodle web-service API
   */
  CoreWebserviceApi coreWebservice();

  /**
   * @return the Moodle user grade-report API
   */
  GradeReportUserApi gradeReportUser();

  /**
   * @return the Moodle forum API
   */
  ModForumApi modForum();

  /**
   * @return the Moodle mobile API
   */
  ToolMobileApi toolMobile();

  /**
   * Requests a web-service token from Moodle.
   *
   * @param request token request data
   * @return Moodle token response
   */
  LoginTokenResponseApi login(LoginTokenRequestApi request);

  /**
   * Executes a Moodle request and maps its response to the requested type.
   *
   * @param request request data
   * @param responseType response model class
   * @param <T> response model type
   * @return mapped response
   */
  <T> T execute(Object request, Class<T> responseType);

  /**
   * Executes a Moodle request and maps its response array.
   *
   * @param request request data
   * @param elementType response element class
   * @param <T> response element type
   * @return mapped response elements
   */
  <T> List<T> executeList(Object request, Class<T> elementType);

  /** Executes one AJAX request using Moodle's default AJAX endpoint. */
  default <T> AjaxResponse<T> executeAjax(AjaxRequest<?> request, Class<T> responseType) {
    return executeAjax("/lib/ajax/service.php", List.of(request), responseType);
  }

  /**
   * Executes AJAX requests against the specified Moodle context.
   *
   * @param context AJAX endpoint path
   * @param requests requests to execute
   * @param responseType response data class
   * @param <T> response data type
   * @return the first mapped AJAX response
   */
  <T> AjaxResponse<T> executeAjax(
      String context, List<? extends AjaxRequest<?>> requests, Class<T> responseType);

  /** Executes multiple AJAX requests using Moodle's default AJAX endpoint. */
  default <T> List<AjaxResponse<T>> executeAjaxList(
      List<? extends AjaxRequest<?>> requests, Class<T> elementType) {
    return executeAjaxList("/lib/ajax/service.php", requests, elementType);
  }

  /**
   * Executes multiple AJAX requests against the specified Moodle context.
   *
   * @param context AJAX endpoint path
   * @param requests requests to execute
   * @param elementType response data class
   * @param <T> response data type
   * @return mapped AJAX responses
   */
  <T> List<AjaxResponse<T>> executeAjaxList(
      String context, List<? extends AjaxRequest<?>> requests, Class<T> elementType);
}
