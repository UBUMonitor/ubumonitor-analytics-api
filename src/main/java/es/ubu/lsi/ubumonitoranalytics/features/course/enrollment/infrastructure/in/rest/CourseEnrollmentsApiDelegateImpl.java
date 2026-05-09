package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.CourseEnrollmentsApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseEnrollmentsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.in.GetCourseEnrollmentsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.application.port.in.SyncCourseEnrollmentsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollment;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseEnrollmentsApiDelegateImpl implements CourseEnrollmentsApiDelegate {



    private final SyncCourseEnrollmentsUseCase syncCourseEnrollmentsUseCase;
    private final GetCourseEnrollmentsUseCase getCourseEnrollmentsUseCase;
    private final CourseEnrollmentsMapper courseEnrollmentsMapper;


    @Override
    public ResponseEntity<CourseEnrollmentsResponseDto> syncCourseUsersEnrollments(Integer courseId) {
        CourseEnrollment enrollments = syncCourseEnrollmentsUseCase.syncCourseEnrollments(courseId);

        return ResponseEntity.ok(courseEnrollmentsMapper.toDto(enrollments));
    }

    @Override
    public ResponseEntity<CourseEnrollmentsResponseDto> getCourseUsersEnrollmentsInfo(Integer courseId) {
        CourseEnrollment enrollments = getCourseEnrollmentsUseCase.getCourseEnrollment(courseId);
        return ResponseEntity.ok(courseEnrollmentsMapper.toDto(enrollments));
    }

}
