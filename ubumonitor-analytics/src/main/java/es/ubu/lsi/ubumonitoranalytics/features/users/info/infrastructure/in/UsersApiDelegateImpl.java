package es.ubu.lsi.ubumonitoranalytics.features.users.info.infrastructure.in;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.UsersApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CurrentUserInfoResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.in.GenerateEnrollmentImagesDocumentUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.in.GetUserProfileImageUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.in.SyncUserInfoUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.document.EnrollmentImagesDocumentCommand;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.image.UserImage;
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
  private final SyncUserInfoUseCase syncUserInfoUseCase;

  private final UserInfoMapper userInfoMapper;
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
      URI defaultImageUri,
      Boolean useDefaultPhoto,
      String headerText,
      String fileName) {

    EnrollmentImagesDocumentCommand command =
        enrollmentImagesDocumentCommandMapper.toDomain(
            userIds, defaultImageUri, useDefaultPhoto, headerText);

    byte[] docx = generateEnrollmentImagesDocumentUseCase.generateEnrollmentImagesDocument(command);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentDisposition(
        ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build());

    return ResponseEntity.ok().headers(headers).body(new ByteArrayResource(docx));
  }

  @Override
  public ResponseEntity<CurrentUserInfoResponseDto> getCurrentUserInfo() {
    return ResponseEntity.ok(userInfoMapper.toDto(syncUserInfoUseCase.syncActualUser()));
  }
}
