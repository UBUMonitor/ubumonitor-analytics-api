package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.out.moodle;

import es.ubu.lsi.moodleadapter.api.generated.api.CoursesApi;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterCourseUsersResponseDto;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterGetCourseUsersOptionsParameterDto;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.out.EnrollmentFetchPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollment;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.moodle.MoodleDownloaderPort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.UserPicture;
import es.ubu.lsi.ubumonitoranalytics.util.HashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


import java.util.List;
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
    public CourseEnrollment fetchCourseEnrolledUsers(Integer courseId, String token) {
        MoodleAdapterCourseUsersResponseDto response = coursesApi.getCourseUsers(courseId, new MoodleAdapterGetCourseUsersOptionsParameterDto());
        CourseEnrollment courseEnrollment = courseUsersMapper.toDomain(response, courseId);
        downloadImages(courseEnrollment, token);

        return courseEnrollment;
    }

    private void downloadImages(CourseEnrollment courseEnrollment, String token) {
        List<CompletableFuture<UserPicture>> futures = courseEnrollment
            .getEnrollments()
            .stream()
            .map(Enrollment::getUser)
            .map(User::getUserPicture)
            .map(userPicture -> CompletableFuture.supplyAsync(() ->
                fetchUserImage(userPicture, token), executor
            ))
            .toList();

        futures.forEach(CompletableFuture::join);

    }

    public UserPicture fetchUserImage(UserPicture userPicture, String token) {
        byte[] image = moodleDownloaderPort.downloadUserImage(userPicture.getUrl(), token);
        userPicture.setData(image);
        String hexHash = HashUtil.imageHash(image);
        userPicture.setHexHash(hexHash);
        return userPicture;
    }


}
