package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.CourseEnrollmentsApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseEnrollmentsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.in.GetCourseEnrollmentsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.application.port.in.SyncCourseEnrollmentsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model.UsersResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseEnrollmentsApiDelegateImpl implements CourseEnrollmentsApiDelegate {



    private final SyncCourseEnrollmentsUseCase syncCourseEnrollmentsUseCase;
    private final GetCourseEnrollmentsUseCase getCourseEnrollmentsUseCase;
    private final CourseEnrollmentsApiDelegateMapper courseEnrollmentsApiDelegateMapper;


    @Override
    public ResponseEntity<CourseEnrollmentsResponseDto> syncCourseUsersEnrollments(Integer courseId) {
        UsersResponse usersResponse = syncCourseEnrollmentsUseCase.syncCourseEnrollments(courseId);

        return ResponseEntity.ok(courseEnrollmentsApiDelegateMapper.toDto(usersResponse, courseId));
    }

    @Override
    public ResponseEntity<CourseEnrollmentsResponseDto> getCourseUsersEnrollmentsInfo(Integer courseId) {
        UsersResponse usersResponse = getCourseEnrollmentsUseCase.getCourseEnrollment(courseId);
        return ResponseEntity.ok(courseEnrollmentsApiDelegateMapper.toDto(usersResponse, courseId));
    }

}

