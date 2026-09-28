package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.infrastructure.out.moodle;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.core.enrol.getuserscourses.request.GetUsersCoursesRequestApi;
import es.ubu.lsi.moodle.model.core.enrol.getuserscourses.response.GetUsersCoursesResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out.EnrollmentCoursesApiFetchPort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.UserEnrolledCourses;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MoodleUserCoursesApiAdapter implements EnrollmentCoursesApiFetchPort {

  private final Client client;
  private final MoodleUserCoursesAdapterMapper moodleUserCoursesAdapterMapper;

  @Override
  public UserEnrolledCourses fetchEnrolledCourses(Integer userId) {
    GetUsersCoursesRequestApi request = moodleUserCoursesAdapterMapper.toRequest(userId);
    List<GetUsersCoursesResponseApi> response = client.coreEnrol().getUsersCourses(request);
    return moodleUserCoursesAdapterMapper.toDomain(userId, response);
  }
}
