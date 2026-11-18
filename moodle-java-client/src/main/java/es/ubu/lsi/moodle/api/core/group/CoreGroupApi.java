package es.ubu.lsi.moodle.api.core.group;

import es.ubu.lsi.moodle.model.core.group.getgroupmembers.request.GetGroupMembersRequestApi;
import es.ubu.lsi.moodle.model.core.group.getgroupmembers.response.GetGroupMembersResponseApi;
import java.util.List;

/** Exposes Moodle group operations. */
public interface CoreGroupApi {

  /**
   * @param request query for group members
   * @return the members of the requested groups
   */
  List<GetGroupMembersResponseApi> getGroupMembers(GetGroupMembersRequestApi request);
}
