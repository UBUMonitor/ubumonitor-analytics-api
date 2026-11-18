package es.ubu.lsi.ubumonitoranalytics.features.users.info.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;

public interface SearchUserByUserNameUseCase {

  UserInfo searchUserByUserName(String userName);
}
