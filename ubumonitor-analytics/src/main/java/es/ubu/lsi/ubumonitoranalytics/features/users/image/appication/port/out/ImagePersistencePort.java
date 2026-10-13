package es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.model.UserImage;

/** Reads profile image content and metadata from tenant persistence. */
public interface ImagePersistencePort {
  /**
   * @param userId Moodle user identifier
   * @return stored image hash, or {@code null} when no image exists
   */
  String getImageHash(Integer userId);

  /**
   * @param userId Moodle user identifier
   * @return stored profile image
   */
  UserImage fetchUserImage(Integer userId);
}
