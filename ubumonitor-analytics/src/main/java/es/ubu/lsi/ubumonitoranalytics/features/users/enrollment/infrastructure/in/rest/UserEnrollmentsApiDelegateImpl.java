package es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.UserEnrollmentsApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.EnrollmentsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.application.port.in.GetUserCoursesUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.application.port.in.SyncUserCoursesUseCase;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.security.RequiresOnlineSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/** Implements the generated REST operations for current-user enrollments. */
@Component
@RequiredArgsConstructor
public class UserEnrollmentsApiDelegateImpl implements UserEnrollmentsApiDelegate {

  private final SyncUserCoursesUseCase syncUserCoursesUseCase;
  private final GetUserCoursesUseCase getUserCoursesUseCase;
  private final UserCoursesEnrollmentsMapper userCoursesEnrollmentsMapper;

  @Override
  @RequiresOnlineSession
  public ResponseEntity<EnrollmentsResponseDto> syncCurrentUserEnrollments() {
    return ResponseEntity.ok(
        userCoursesEnrollmentsMapper.toDto(syncUserCoursesUseCase.syncActualUserEnrollments()));
  }

  @Override
  public ResponseEntity<EnrollmentsResponseDto> getCurrentUserEnrollments() {
    return ResponseEntity.ok(
        userCoursesEnrollmentsMapper.toDto(getUserCoursesUseCase.getActualUserEnrollments()));
  }
}
