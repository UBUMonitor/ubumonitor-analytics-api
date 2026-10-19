package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.list;

import java.util.List;
import lombok.Data;

@Data
public class CourseLogsInfoRequest {
  private Long courseId;
  private CourseLogsInfoRequestTimeRange timeRange;
  private CourseLogsInfoRequestFilters filters;
  private List<LogViewColumn> fields;
  private CourseLogsInfoRequestPagination pagination;
  private List<CourseLogsInfoRequestSortItem> sort;
  private Boolean includeTotal;
}
