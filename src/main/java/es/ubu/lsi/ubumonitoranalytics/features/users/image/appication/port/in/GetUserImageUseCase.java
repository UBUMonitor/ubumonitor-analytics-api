package es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.model.UserImage;

import java.net.URI;

public interface GetUserImageUseCase {

    UserImage getUserImage(Integer userId, URI host, String username, String ifNoneMatch);
}

