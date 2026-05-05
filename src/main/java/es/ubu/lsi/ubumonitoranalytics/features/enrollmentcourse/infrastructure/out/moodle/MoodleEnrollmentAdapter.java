package es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.infrastructure.out.moodle;

import es.ubu.lsi.moodleadapter.api.generated.api.CoursesApi;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterCourseUsersResponseDto;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterGetCourseUsersOptionsParameterDto;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.application.port.out.EnrollmentFetchPort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.domain.model.CourseEnrollment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MoodleEnrollmentAdapter implements EnrollmentFetchPort {

    private final CoursesApi coursesApi;
    private final CourseUsersMapper courseUsersMapper;

    @Override
    public CourseEnrollment fetchCourseEnrolledUsers(Integer courseId) {
        MoodleAdapterCourseUsersResponseDto response = coursesApi.getCourseUsers(courseId, new MoodleAdapterGetCourseUsersOptionsParameterDto());
        return courseUsersMapper.toDomain(response, courseId);
    }
}
