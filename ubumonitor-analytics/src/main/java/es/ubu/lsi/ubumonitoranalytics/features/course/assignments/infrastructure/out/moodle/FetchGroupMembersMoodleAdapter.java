package es.ubu.lsi.ubumonitoranalytics.features.course.assignments.infrastructure.out.moodle;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.core.group.getgroupmembers.response.GetGroupMembersResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.application.port.out.FetchGroupMembersPort;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FetchGroupMembersMoodleAdapter implements FetchGroupMembersPort {

  private final Client client;
  private final GroupMembersMoodleMapper mapper;

  @Override
  public Map<Long, List<Long>> fetchGroupMembers(Set<Long> groupIds) {
    if (groupIds.isEmpty()) {
      return Map.of();
    }

    List<GetGroupMembersResponseApi> response =
        client
            .coreGroup()
            .getGroupMembers(mapper.toRequest(new IdsRequest<>(List.copyOf(groupIds))));
    if (response == null) {
      return Map.of();
    }
    return mapper.toDomain(response);
  }
}
