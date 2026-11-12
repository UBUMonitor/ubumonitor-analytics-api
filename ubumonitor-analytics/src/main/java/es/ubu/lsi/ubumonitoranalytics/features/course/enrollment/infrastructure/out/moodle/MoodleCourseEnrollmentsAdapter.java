package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.moodle;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.core.enrol.getenrolledusers.request.GetEnrolledUsersArgsApi;
import es.ubu.lsi.moodle.model.core.enrol.getenrolledusers.request.GetEnrolledUsersRequestApi;
import es.ubu.lsi.moodle.model.core.enrol.getenrolledusers.response.GetEnrolledUsersResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.out.FetchCourseEnrollmentsPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollments;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.concurrent.AsyncExecutionPort;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.moodle.MoodleDownloaderPort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.UserPicture;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.util.HashUtil;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

/** Fetches course enrollment data and profile images from Moodle. */
@Slf4j
@Component
@RequiredArgsConstructor
public class MoodleCourseEnrollmentsAdapter implements FetchCourseEnrollmentsPort {

  private static final MimeType ANY_IMAGE = MimeType.valueOf("image/*");

  private final Client client;
  private final EnrolledUsersMapper enrolledUsersMapper;
  private final MoodleDownloaderPort moodleDownloaderPort;
  private final AsyncExecutionPort asyncExecution;

  @Override
  public CourseEnrollments fetchCourseEnrollments(Long courseId, String token) {
    GetEnrolledUsersArgsApi args = new GetEnrolledUsersArgsApi();
    args.setCourseid(courseId);
    GetEnrolledUsersRequestApi request = new GetEnrolledUsersRequestApi();
    request.setArgs(args);

    List<GetEnrolledUsersResponseApi> response = client.coreEnrol().getEnrolledUsers(request);
    CourseEnrollments enrollments = enrolledUsersMapper.toDomain(courseId, response);

    downloadProfileImages(enrollments.users(), token);
    return enrollments;
  }

  /** Downloads all profile images concurrently and waits for every download to finish. */
  private void downloadProfileImages(List<User> users, String token) {
    CompletableFuture<?>[] downloads =
        users.stream()
            .map(User::getUserPicture)
            .filter(picture -> picture != null && picture.getUrl() != null)
            .map(picture -> asyncExecution.runAsync(() -> downloadProfileImage(picture, token)))
            .toArray(CompletableFuture<?>[]::new);

    CompletableFuture.allOf(downloads).join();
  }

  /** A missing picture must not fail the whole synchronization, so errors are only logged. */
  private void downloadProfileImage(UserPicture picture, String token) {
    try {
      ResponseEntity<byte[]> image =
          moodleDownloaderPort.downloadUserImage(picture.getUrl(), token);

      MediaType contentType = image.getHeaders().getContentType();
      byte[] body = image.getBody();
      if (image.getStatusCode().is2xxSuccessful()
          && body != null
          && contentType != null
          && ANY_IMAGE.isCompatibleWith(contentType)) {
        picture.setData(body);
        picture.setContentType(contentType.toString());
        picture.setHexHash(HashUtil.imageHash(body));
      }
    } catch (RuntimeException e) {
      log.warn("Could not download profile image from {}", picture.getUrl(), e);
    }
  }
}
