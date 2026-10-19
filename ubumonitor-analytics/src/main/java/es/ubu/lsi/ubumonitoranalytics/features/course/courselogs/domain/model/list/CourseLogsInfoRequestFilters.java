package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.list;

import java.util.List;
import lombok.Data;

@Data
public class CourseLogsInfoRequestFilters {

  private List<Long> userIds;
  private List<Long> moduleIds;
  private List<Short> componentIds;
  private List<Short> eventIds;
  private List<Byte> originIds;
  private List<Long> sectionIds;
  private List<String> ipAddresses;
}
