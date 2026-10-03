package es.ubu.lsi.moodle.api.core.course;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.core.course.getcontents.request.GetCourseContentsRequestApi;
import es.ubu.lsi.moodle.model.core.course.getcontents.response.GetCourseContentsResponseApi;
import java.util.List;
import lombok.RequiredArgsConstructor;

/** Implements Moodle course calls through the shared client. */
@RequiredArgsConstructor
public class CoreCourseApiImpl implements CoreCourseApi {
  private final Client client;

  @Override
  public List<GetCourseContentsResponseApi> getContents(GetCourseContentsRequestApi request) {
    return client.executeList(request, GetCourseContentsResponseApi.class);
  }
}
