package es.ubu.lsi.ubumonitoranalytics.features.users.image.infrastructure.in;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.in.GenerateEnrollmentImagesDocumentUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.in.GetUserProfileImageUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.model.UserImage;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class UsersApiDelegateImplTest {

  @Test
  void requiresRevalidationForModifiedAndUnmodifiedImages() {
    GetUserProfileImageUseCase getUserProfileImageUseCase = mock(GetUserProfileImageUseCase.class);
    when(getUserProfileImageUseCase.getUserImage(13, null, "teacher", null))
        .thenReturn(
            UserImage.builder()
                .hexHash("image-hash")
                .contentType("image/png")
                .image(new byte[] {1})
                .isModified(true)
                .build(),
            UserImage.builder().hexHash("image-hash").isModified(false).build());

    UsersApiDelegateImpl delegate =
        new UsersApiDelegateImpl(
            getUserProfileImageUseCase,
            mock(GenerateEnrollmentImagesDocumentUseCase.class),
            mock(EnrollmentImagesDocumentCommandMapper.class));

    ResponseEntity<Resource> modifiedResponse =
        delegate.getUserImagesProfile(13, null, "teacher", null);
    ResponseEntity<Resource> unmodifiedResponse =
        delegate.getUserImagesProfile(13, null, "teacher", null);

    assertEquals(HttpStatus.OK, modifiedResponse.getStatusCode());
    assertEquals(HttpStatus.NOT_MODIFIED, unmodifiedResponse.getStatusCode());
    assertTrue(modifiedResponse.getHeaders().getCacheControl().contains("no-cache"));
    assertTrue(unmodifiedResponse.getHeaders().getCacheControl().contains("no-cache"));
  }
}
