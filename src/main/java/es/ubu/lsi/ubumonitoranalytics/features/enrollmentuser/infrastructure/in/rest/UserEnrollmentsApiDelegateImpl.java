package es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.UserEnrollmentsApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.EnrollmentsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.application.port.in.SyncUserCoursesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEnrollmentsApiDelegateImpl implements UserEnrollmentsApiDelegate {

    private final SyncUserCoursesUseCase syncUserCoursesUseCase;
    private final SyncEnrollmentsMapper syncEnrollmentsMapper;



    @Override
    public ResponseEntity<EnrollmentsResponseDto> syncUserEnrollments() {
        return ResponseEntity.ok(syncEnrollmentsMapper.toDto(syncUserCoursesUseCase.syncActualUserEnrollments()));
    }

    @Override
    public ResponseEntity<EnrollmentsResponseDto> getActualUserEnrollments() {
        return ResponseEntity.ok(null);
    }


}
