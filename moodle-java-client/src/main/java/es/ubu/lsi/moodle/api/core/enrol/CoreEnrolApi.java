package es.ubu.lsi.moodle.api.core.enrol;

import es.ubu.lsi.moodle.model.core.enrol.getenrolledusers.request.GetEnrolledUsersRequestApi;
import es.ubu.lsi.moodle.model.core.enrol.getenrolledusers.response.GetEnrolledUsersResponseApi;
import es.ubu.lsi.moodle.model.core.enrol.getuserscourses.request.GetUsersCoursesRequestApi;
import es.ubu.lsi.moodle.model.core.enrol.getuserscourses.response.GetUsersCoursesResponseApi;
import java.util.List;

public interface CoreEnrolApi {

  List<GetUsersCoursesResponseApi> getUsersCourses(GetUsersCoursesRequestApi request);

  List<GetEnrolledUsersResponseApi> getEnrolledUsers(GetEnrolledUsersRequestApi request);
}
