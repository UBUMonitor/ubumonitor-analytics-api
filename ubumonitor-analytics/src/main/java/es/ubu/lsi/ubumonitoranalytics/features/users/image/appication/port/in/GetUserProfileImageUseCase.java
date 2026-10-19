package es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.model.UserImage;
import java.net.URI;

/** Retrieves a user's profile image and its modification state. */
public interface GetUserProfileImageUseCase {

  /**
   * @param userId Moodle user identifier
   * @param host Moodle site URI
   * @param username Moodle username
   * @param ifNoneMatch ETag from a previous response, if available
   * @return the image and whether it changed since the supplied ETag
   */
  UserImage getUserImage(Long userId, URI host, String username, String ifNoneMatch);
}
