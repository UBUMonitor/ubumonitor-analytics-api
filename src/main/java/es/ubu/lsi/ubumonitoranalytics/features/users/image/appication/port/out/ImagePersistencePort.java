package es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.model.UserImage;

public interface ImagePersistencePort {
  String getImageHash(Integer userId);

  UserImage fetchUserImage(Integer userId);
}
