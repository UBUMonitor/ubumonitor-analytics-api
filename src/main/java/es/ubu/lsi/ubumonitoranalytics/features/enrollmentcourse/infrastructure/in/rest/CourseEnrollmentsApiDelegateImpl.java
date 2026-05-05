package es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.CourseEnrollmentsApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseEnrollmentsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.application.port.in.GetCourseEnrollmentsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.application.port.in.SyncCourseEnrollmentsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.domain.model.CourseEnrollment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseEnrollmentsApiDelegateImpl implements CourseEnrollmentsApiDelegate {



    private final SyncCourseEnrollmentsUseCase syncCourseEnrollmentsUseCase;
    private final GetCourseEnrollmentsUseCase getCourseEnrollmentsUseCase;
    private final CourseEnrollmentsMapper courseEnrollmentsMapper;


    @Override
    public CourseEnrollmentsResponseDto syncCourseUsersEnrollments(Integer courseId) {
        CourseEnrollment enrollments = syncCourseEnrollmentsUseCase.syncCourseEnrollments(courseId);

        return courseEnrollmentsMapper.toDto(enrollments);
    }

    @Override
    public CourseEnrollmentsResponseDto getCourseUsersEnrollmentsInfo(Integer courseId) {
        CourseEnrollment enrollments = getCourseEnrollmentsUseCase.getCourseEnrollment(courseId);
        return courseEnrollmentsMapper.toDto(enrollments);
    }

}
