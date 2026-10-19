package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics;

import java.util.List;
import lombok.Data;

@Data
public class CourseLogsStatisticsRequestFilters {

  private List<Integer> userIds;
  private List<Integer> moduleIds;
  private List<Short> componentIds;
  private List<Short> eventIds;
  private List<Byte> originIds;
  private List<String> ipAddresses;
  private List<Integer> sectionIds;
}
