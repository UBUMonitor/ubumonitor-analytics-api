package es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.model.UserImage;

import java.util.Optional;

public interface GetUserImageUseCase {

    Optional<UserImage> getUserImage(Integer userId, String ifNoneMatch);
}
