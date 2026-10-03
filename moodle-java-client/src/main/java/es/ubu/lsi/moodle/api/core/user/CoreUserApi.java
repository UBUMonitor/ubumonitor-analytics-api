package es.ubu.lsi.moodle.api.core.user;

import es.ubu.lsi.moodle.model.core.user.getusersbyfield.request.GetUsersByFieldRequestApi;
import es.ubu.lsi.moodle.model.core.user.getusersbyfield.response.GetUsersByFieldResponseApi;
import java.util.List;

/** Exposes Moodle core user operations. */
public interface CoreUserApi {
  /**
   * @param request user lookup criteria
   * @return users matching the requested field and value
   */
  List<GetUsersByFieldResponseApi> getUsersByField(GetUsersByFieldRequestApi request);
}
