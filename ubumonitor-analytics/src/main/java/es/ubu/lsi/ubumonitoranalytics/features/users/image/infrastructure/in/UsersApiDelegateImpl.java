package es.ubu.lsi.ubumonitoranalytics.features.users.image.infrastructure.in;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.UsersApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.in.GenerateEnrollmentImagesDocumentUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.in.GetUserProfileImageUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.EnrollmentImagesDocumentCommand;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.model.UserImage;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

/** Implements the generated REST operation for user profile images. */
@Component
@RequiredArgsConstructor
public class UsersApiDelegateImpl implements UsersApiDelegate {

  private final GetUserProfileImageUseCase getUserProfileImageUseCase;
  private final GenerateEnrollmentImagesDocumentUseCase generateEnrollmentImagesDocumentUseCase;

  private final EnrollmentImagesDocumentCommandMapper enrollmentImagesDocumentCommandMapper;

  @Override
  public ResponseEntity<Resource> getUserImagesProfile(
      Long userId, URI host, String username, String ifNoneMatch) {
    UserImage userImage =
        getUserProfileImageUseCase.getUserImage(userId, host, username, ifNoneMatch);
    CacheControl cacheControl = CacheControl.noCache().cachePublic();

    if (!userImage.isModified()) {
      return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
          .eTag(userImage.getHexHash())
          .cacheControl(cacheControl)
          .build();
    }

    return ResponseEntity.ok()
        .eTag(userImage.getHexHash())
        .cacheControl(cacheControl)
        .contentType(MediaType.parseMediaType(userImage.getContentType()))
        .contentLength(userImage.getImage().length)
        .body(new ByteArrayResource(userImage.getImage()));
  }

  @Override
  public ResponseEntity<Resource> getUsersEnrollmentImagesDocument(
      List<Long> userIds,
      String defaultImagePath,
      Boolean useDefaultPhoto,
      String headerText,
      String fileName) {

    EnrollmentImagesDocumentCommand command =
        enrollmentImagesDocumentCommandMapper.toDomain(
            userIds, defaultImagePath, useDefaultPhoto, headerText);

    byte[] docx = generateEnrollmentImagesDocumentUseCase.generateEnrollmentImagesDocument(command);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentDisposition(
        ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build());

    return ResponseEntity.ok().headers(headers).body(new ByteArrayResource(docx));
  }
}
