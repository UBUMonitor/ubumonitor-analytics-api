package es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.UserEnrollmentsApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.EnrollmentsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.application.port.in.UserCoursesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEnrollmentsApiDelegateImpl implements UserEnrollmentsApiDelegate {

    private final UserCoursesUseCase userCoursesUseCase;
    private final SyncEnrollmentsMapper syncEnrollmentsMapper;



    @Override
    public EnrollmentsResponseDto syncUserEnrollments() {
        return syncEnrollmentsMapper.toDto(userCoursesUseCase.syncActualUserEnrollments());
    }

    @Override
    public EnrollmentsResponseDto getActualUserEnrollments() {
        return null;
    }


}
