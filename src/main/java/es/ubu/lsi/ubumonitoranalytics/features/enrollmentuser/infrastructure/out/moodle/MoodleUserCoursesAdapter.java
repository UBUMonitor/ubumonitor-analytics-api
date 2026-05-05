package es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.infrastructure.out.moodle;

import es.ubu.lsi.moodleadapter.api.generated.api.UsersApi;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.application.port.out.EnrollmentCoursesFetchPort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.domain.model.UserCourses;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MoodleUserCoursesAdapter implements EnrollmentCoursesFetchPort {

    private final UsersApi usersApi;
    private final MoodleUserCoursesAdapterMapper moodleUserCoursesAdapterMapper;

    @Override
    public UserCourses fetchEnrolledCourses(Integer userId) {
        return moodleUserCoursesAdapterMapper.toDomain(userId, usersApi.getUserCourses(userId, false));
    }

}
