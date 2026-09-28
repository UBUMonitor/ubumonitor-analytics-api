package es.ubu.lsi.moodle.api.core.enrol;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.core.enrol.getenrolledusers.request.GetEnrolledUsersRequestApi;
import es.ubu.lsi.moodle.model.core.enrol.getenrolledusers.response.GetEnrolledUsersResponseApi;
import es.ubu.lsi.moodle.model.core.enrol.getuserscourses.request.GetUsersCoursesRequestApi;
import es.ubu.lsi.moodle.model.core.enrol.getuserscourses.response.GetUsersCoursesResponseApi;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CoreEnrolApiImpl implements CoreEnrolApi {
  private final Client client;

  @Override
  public List<GetUsersCoursesResponseApi> getUsersCourses(GetUsersCoursesRequestApi request) {
    return client.executeList(request, GetUsersCoursesResponseApi.class);
  }

  @Override
  public List<GetEnrolledUsersResponseApi> getEnrolledUsers(GetEnrolledUsersRequestApi request) {
    return client.executeList(request, GetEnrolledUsersResponseApi.class);
  }
}
