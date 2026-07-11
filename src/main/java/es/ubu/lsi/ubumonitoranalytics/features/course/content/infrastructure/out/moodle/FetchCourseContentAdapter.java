package es.ubu.lsi.ubumonitoranalytics.features.course.content.infrastructure.out.moodle;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.core.course.getcontents.request.GetCourseContentsRequestApi;
import es.ubu.lsi.moodle.model.core.course.getcontents.response.GetCourseContentsResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.out.FetchCourseContentPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseContent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FetchCourseContentAdapter implements FetchCourseContentPort {

  private final Client client;
  private final FetchCourseContentAdapaterMapper fetchCourseContentAdapaterMapper;

  @Override
  public CourseContent fetchCourseContent(Integer courseId) {
    GetCourseContentsRequestApi request = fetchCourseContentAdapaterMapper.toRequest(courseId);
    List<GetCourseContentsResponseApi> response = client.coreCourse().getContents(request);
    return fetchCourseContentAdapaterMapper.toDomain(response, courseId);
  }
}
