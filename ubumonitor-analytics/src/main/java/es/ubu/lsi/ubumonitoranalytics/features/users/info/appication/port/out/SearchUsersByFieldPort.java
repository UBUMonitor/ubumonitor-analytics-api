package es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;

public interface SearchUsersByFieldPort {
  UserInfo getUserByUserName(String userName);
}
