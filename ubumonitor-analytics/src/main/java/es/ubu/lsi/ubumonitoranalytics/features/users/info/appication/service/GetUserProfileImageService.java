package es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.service;

import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.in.GetUserProfileImageUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.out.UserProfileImagePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.image.UserImage;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.SessionStorePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.util.ImageUtil;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Retrieves profile images and determines whether the supplied ETag is current. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserProfileImageService implements GetUserProfileImageUseCase {

  private final UserProfileImagePersistencePort imagePersistencePort;
  private final SessionStorePort sessionStorePort;

  /**
   * @param userId Moodle user identifier
   * @param host Moodle site URI
   * @param username Moodle username
   * @param ifNoneMatch request ETag, if supplied
   * @return image content and modification state, or the unchanged state
   */
  @Override
  public UserImage getUserImage(Long userId, URI host, String username, String ifNoneMatch) {
    String normalized = normalize(ifNoneMatch);
    SessionData sessionData = sessionStorePort.getSession(host, username);

    if (sessionData == null) {
      return getPlaceHolder(normalized);
    }

    String imageHash = imagePersistencePort.getImageHash(userId, sessionData);

    // Return the placeholder when the user has no stored image.
    if (imageHash == null) {
      return getPlaceHolder(normalized);
    }

    // Return an unmodified result when the ETag matches.
    if (imageHash.equals(normalized)) {
      return UserImage.builder().hexHash(imageHash).image(null).isModified(false).build();
    }

    UserImage userImage = imagePersistencePort.fetchUserImage(userId, sessionData);
    userImage.setModified(true);

    return userImage;
  }

  private static UserImage getPlaceHolder(String normalized) {
    String etag = ImageUtil.getPlaceholderEtag();
    boolean modified = !etag.equals(normalized);

    return UserImage.builder()
        .hexHash(etag)
        .contentType("image/png")
        .image(modified ? ImageUtil.getPlaceholder() : null)
        .isModified(modified)
        .build();
  }

  private String normalize(String ifNoneMatch) {

    if (ifNoneMatch == null) {
      return null;
    }

    return ifNoneMatch.replace("\"", "");
  }
}
