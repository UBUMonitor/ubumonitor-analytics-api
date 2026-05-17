package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.out.moodle;

import es.ubu.lsi.moodleadapter.api.generated.api.CoursesApi;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterCourseUsersResponseDto;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterGetCourseUsersOptionsParameterDto;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.out.EnrollmentFetchPort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.UsersResponse;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.moodle.MoodleDownloaderPort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.UserPicture;
import es.ubu.lsi.ubumonitoranalytics.util.HashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
@RequiredArgsConstructor
public class MoodleEnrollmentAdapter implements EnrollmentFetchPort {

    private final CoursesApi coursesApi;
    private final CourseUsersMapper courseUsersMapper;
    private final MoodleDownloaderPort moodleDownloaderPort;
    private final Executor executor;

    @Override
    public UsersResponse fetchCourseEnrolledUsers(Integer courseId, String token) {
        MoodleAdapterCourseUsersResponseDto response = coursesApi.getCourseUsers(courseId, new MoodleAdapterGetCourseUsersOptionsParameterDto());
        UsersResponse usersResponse = courseUsersMapper.toDomain(response, courseId);
        downloadImages(usersResponse, token);

        return usersResponse;
    }

    private void downloadImages(UsersResponse usersResponse, String token) {
        List<CompletableFuture<UserPicture>> futures = usersResponse
            .getUsers()
            .stream()
            .map(User::getUserPicture)
            .map(userPicture -> CompletableFuture.supplyAsync(() ->
                fetchUserImage(userPicture, token), executor
            ))
            .toList();

        futures.forEach(CompletableFuture::join);

    }

    public UserPicture fetchUserImage(UserPicture userPicture, String token) {
        ResponseEntity<byte[]> image = moodleDownloaderPort.downloadUserImage(userPicture.getUrl(), token);
        if (image.getStatusCode().is2xxSuccessful()) {
            byte[] body = image.getBody();
            userPicture.setData(body);
            userPicture.setContentType(Optional.ofNullable(image.getHeaders().getContentType()).map(Object::toString).orElse("image/png"));
            String hexHash = HashUtil.imageHash(body);
            userPicture.setHexHash(hexHash);
        }

        return userPicture;
    }


}

