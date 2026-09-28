package es.ubu.lsi.moodle.api.core.user;

import es.ubu.lsi.moodle.model.core.user.getusersbyfield.request.GetUsersByFieldRequestApi;
import es.ubu.lsi.moodle.model.core.user.getusersbyfield.response.GetUsersByFieldResponseApi;
import java.util.List;

public interface CoreUserApi {
  List<GetUsersByFieldResponseApi> getUsersByField(GetUsersByFieldRequestApi request);
}
