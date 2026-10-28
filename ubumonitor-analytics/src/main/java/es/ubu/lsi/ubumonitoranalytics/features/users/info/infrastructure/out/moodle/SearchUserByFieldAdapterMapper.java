package es.ubu.lsi.ubumonitoranalytics.features.users.info.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.core.user.getusersbyfield.request.GetUsersByFieldRequestApi;
import es.ubu.lsi.moodle.model.core.user.getusersbyfield.response.GetUsersByFieldResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.SearchField;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.UserInfo;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.time.ZoneId;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = GlobalMapperConfig.class)
public interface SearchUserByFieldAdapterMapper {

  default GetUsersByFieldRequestApi toRequest(SearchField field, String value) {
    return toRequest(field, List.of(value));
  }

  @Mapping(target = "wsfunction", ignore = true)
  @Mapping(target = "args.field", source = "field")
  @Mapping(target = "args.values", source = "values")
  GetUsersByFieldRequestApi toRequest(SearchField field, List<String> values);

  @Mapping(target = "serverTimeZone", source = "timezone", qualifiedByName = "isServerTimeZone")
  @Mapping(target = "userName", source = "username")
  @Mapping(target = "userImageUrl", source = "profileimageurl")
  @Mapping(target = "timeZone", source = "timezone", qualifiedByName = "timezone")
  @Mapping(target = "lastName", source = "lastname")
  @Mapping(target = "initials", source = "initials")
  @Mapping(target = "fullName", source = "fullname")
  @Mapping(target = "firstName", source = "firstname")
  UserInfo toResponse(GetUsersByFieldResponseApi first);

  @Named("isServerTimeZone")
  default boolean isServerTimeZone(String timezone) {
    return timezone == null || "99".equals(timezone);
  }

  @Named("timezone")
  default ZoneId timezone(String timezone) {
    if (timezone == null || "99".equals(timezone)) {
      return null;
    }
    return ZoneId.of(timezone);
  }
}
