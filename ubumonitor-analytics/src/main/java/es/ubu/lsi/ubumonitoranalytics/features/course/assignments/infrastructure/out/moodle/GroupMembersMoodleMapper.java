package es.ubu.lsi.ubumonitoranalytics.features.course.assignments.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.core.group.getgroupmembers.request.GetGroupMembersRequestApi;
import es.ubu.lsi.moodle.model.core.group.getgroupmembers.response.GetGroupMembersResponseApi;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface GroupMembersMoodleMapper {

  @Mapping(target = "wsfunction", ignore = true)
  @Mapping(target = "args.groupids", source = "ids")
  GetGroupMembersRequestApi toRequest(IdsRequest<Long> request);

  default Map<Long, List<Long>> toDomain(List<GetGroupMembersResponseApi> response) {
    Map<Long, List<Long>> membersByGroupId = new HashMap<>();
    if (response == null) {
      return membersByGroupId;
    }
    for (GetGroupMembersResponseApi group : response) {
      if (group != null && group.getGroupid() != null && group.getUserids() != null) {
        membersByGroupId.put(group.getGroupid(), List.copyOf(group.getUserids()));
      }
    }
    return Map.copyOf(membersByGroupId);
  }
}
