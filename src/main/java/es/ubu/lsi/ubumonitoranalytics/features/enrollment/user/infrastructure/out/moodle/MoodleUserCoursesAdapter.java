package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.infrastructure.out.moodle;

import es.ubu.lsi.moodleadapter.api.generated.api.UsersApi;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out.EnrollmentCoursesFetchPort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.UserEnrolledCourses;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MoodleUserCoursesAdapter implements EnrollmentCoursesFetchPort {

    private final UsersApi usersApi;
    private final MoodleUserCoursesAdapterMapper moodleUserCoursesAdapterMapper;

    @Override
    public UserEnrolledCourses fetchEnrolledCourses(Integer userId) {
        return moodleUserCoursesAdapterMapper.toDomain(userId, usersApi.getUserCourses(userId, false));
    }

}

