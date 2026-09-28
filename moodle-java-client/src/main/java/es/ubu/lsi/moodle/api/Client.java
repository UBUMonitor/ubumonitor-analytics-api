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

public interface Client {
  CoreCalendarApi coreCalendar();

  CoreUserApi coreUser();

  CoreCourseApi coreCourse();

  CoreEnrolApi coreEnrol();

  CoreCompletionApi coreCompletion();

  CoreWebserviceApi coreWebservice();

  GradeReportUserApi gradeReportUser();

  ModForumApi modForum();

  ToolMobileApi toolMobile();

  LoginTokenResponseApi login(LoginTokenRequestApi request);

  <T> T execute(Object request, Class<T> responseType);

  <T> List<T> executeList(Object request, Class<T> elementType);

  default <T> AjaxResponse<T> executeAjax(AjaxRequest<?> request, Class<T> responseType) {
    return executeAjax("/lib/ajax/service.php", List.of(request), responseType);
  }

  <T> AjaxResponse<T> executeAjax(
      String context, List<? extends AjaxRequest<?>> requests, Class<T> responseType);

  default <T> List<AjaxResponse<T>> executeAjaxList(
      List<? extends AjaxRequest<?>> requests, Class<T> elementType) {
    return executeAjaxList("/lib/ajax/service.php", requests, elementType);
  }

  <T> List<AjaxResponse<T>> executeAjaxList(
      String context, List<? extends AjaxRequest<?>> requests, Class<T> elementType);
}
