package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.infrastructure.out.moodle;

import es.ubu.lsi.moodleadapter.api.generated.api.UsersApi;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterUserCoursesResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out.EnrollmentCoursesApiFetchPort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.UserEnrolledCourses;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MoodleUserCoursesApiAdapter implements EnrollmentCoursesApiFetchPort {

    private final UsersApi usersApi;
    private final MoodleUserCoursesAdapterMapper moodleUserCoursesAdapterMapper;

    @Override
    public UserEnrolledCourses fetchEnrolledCourses(Integer userId) {
        MoodleAdapterUserCoursesResponseDto response = usersApi.getUserCourses(userId, false);
        return moodleUserCoursesAdapterMapper.toDomain(userId, response);
    }

}

