package es.ubu.lsi.moodle.api.core.group;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.core.group.getgroupmembers.request.GetGroupMembersRequestApi;
import es.ubu.lsi.moodle.model.core.group.getgroupmembers.response.GetGroupMembersResponseApi;
import java.util.List;
import lombok.RequiredArgsConstructor;

/** Implements Moodle group calls through the shared client. */
@RequiredArgsConstructor
public class CoreGroupApiImpl implements CoreGroupApi {
  private final Client client;

  @Override
  public List<GetGroupMembersResponseApi> getGroupMembers(GetGroupMembersRequestApi request) {
    return client.executeList(request, GetGroupMembersResponseApi.class);
  }
}
