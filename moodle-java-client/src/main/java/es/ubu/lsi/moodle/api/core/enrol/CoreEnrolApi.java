package es.ubu.lsi.moodle.api.core.enrol;

import es.ubu.lsi.moodle.model.core.enrol.getenrolledusers.request.GetEnrolledUsersRequestApi;
import es.ubu.lsi.moodle.model.core.enrol.getenrolledusers.response.GetEnrolledUsersResponseApi;
import es.ubu.lsi.moodle.model.core.enrol.getuserscourses.request.GetUsersCoursesRequestApi;
import es.ubu.lsi.moodle.model.core.enrol.getuserscourses.response.GetUsersCoursesResponseApi;
import java.util.List;

/** Exposes Moodle course enrollment operations. */
public interface CoreEnrolApi {

  /**
   * @param request query for a user's courses
   * @return courses associated with the requested users
   */
  List<GetUsersCoursesResponseApi> getUsersCourses(GetUsersCoursesRequestApi request);

  /**
   * @param request query for users enrolled in courses
   * @return enrolled users and their enrollment details
   */
  List<GetEnrolledUsersResponseApi> getEnrolledUsers(GetEnrolledUsersRequestApi request);
}
