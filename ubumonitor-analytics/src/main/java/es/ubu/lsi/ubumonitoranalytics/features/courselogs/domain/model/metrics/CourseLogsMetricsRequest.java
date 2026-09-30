package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics;

import java.util.List;
import lombok.Data;

/** Defines the requested grouping, filtering, and interval for course log metrics. */
@Data
public class CourseLogsMetricsRequest {

  private Integer courseId;

  private CourseLogsMetricsRequestTimeRange timeRange;

  private List<CourseLogsGroupBy> groupBy; // Defines the base grouping dimension.

  private TimeInterval interval;
  private FillGapStrategy fillGapsStrategy;
  private List<CourseLogsSelectColumn> fields;

  private CourseLogsMetricsRequestFilters filters;
  private List<SortBy> sortBy;
}
