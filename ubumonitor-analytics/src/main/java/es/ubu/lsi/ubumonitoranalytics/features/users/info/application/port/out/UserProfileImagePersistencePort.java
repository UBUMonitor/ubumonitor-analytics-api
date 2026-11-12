package es.ubu.lsi.ubumonitoranalytics.features.users.info.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.image.UserImage;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;

/** Reads profile image content and metadata from tenant persistence. */
public interface UserProfileImagePersistencePort {
  /**
   * @param userId Moodle user identifier
   * @return stored image hash, or {@code null} when no image exists
   */
  String getImageHash(Long userId, SessionData sessionData);

  /**
   * @param userId Moodle user identifier
   * @return stored profile image
   */
  UserImage fetchUserImage(Long userId, SessionData sessionData);
}
