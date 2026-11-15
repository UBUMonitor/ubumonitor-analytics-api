package es.ubu.lsi.ubumonitoranalytics.features.course.assignments.application.port.out;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FetchGroupMembersPort {
  Map<Long, List<Long>> fetchGroupMembers(Set<Long> groupIds);
}
