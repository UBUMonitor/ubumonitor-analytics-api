package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.moodle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.api.core.enrol.CoreEnrolApi;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollments;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.concurrent.AsyncExecutionPort;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.moodle.MoodleDownloaderPort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.UserPicture;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.web.client.ResourceAccessException;

@ExtendWith(OutputCaptureExtension.class)
class MoodleCourseEnrollmentsAdapterTest {

  @Test
  void logsImageFailureWithoutThePictureUrlOrException(CapturedOutput output) {
    URI pictureUrl = URI.create("https://moodle.example.test/image.png?token=secret-sentinel");
    UserPicture picture = UserPicture.builder().url(pictureUrl).build();
    User user = new User(13L, "User", "First", "Last", null, null, null, picture);
    CourseEnrollments enrollments =
        new CourseEnrollments(42L, List.of(new Enrollment(user, List.of(), List.of(), List.of())));

    Client client = mock(Client.class);
    CoreEnrolApi coreEnrolApi = mock(CoreEnrolApi.class);
    when(client.coreEnrol()).thenReturn(coreEnrolApi);
    when(coreEnrolApi.getEnrolledUsers(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());

    EnrolledUsersMapper mapper = mock(EnrolledUsersMapper.class);
    when(mapper.toDomain(eq(42L), anyList())).thenReturn(enrollments);

    MoodleDownloaderPort downloader = mock(MoodleDownloaderPort.class);
    when(downloader.downloadUserImage(pictureUrl, "secret-sentinel"))
        .thenThrow(new ResourceAccessException("secret-sentinel"));

    AsyncExecutionPort asyncExecution = mock(AsyncExecutionPort.class);
    when(asyncExecution.runAsync(org.mockito.ArgumentMatchers.any(Runnable.class)))
        .thenAnswer(
            invocation -> {
              invocation.<Runnable>getArgument(0).run();
              return CompletableFuture.completedFuture(null);
            });

    MoodleCourseEnrollmentsAdapter adapter =
        new MoodleCourseEnrollmentsAdapter(client, mapper, downloader, asyncExecution);

    adapter.fetchCourseEnrollments(42L, "secret-sentinel");

    assertTrue(output.getAll().contains("type=ResourceAccessException"));
    assertFalse(output.getAll().contains("secret-sentinel"));
    assertFalse(output.getAll().contains("moodle.example.test"));
  }
}
