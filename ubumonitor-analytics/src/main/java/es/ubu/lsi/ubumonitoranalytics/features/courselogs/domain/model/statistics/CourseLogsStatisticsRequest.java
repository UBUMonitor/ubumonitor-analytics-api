package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.statistics;

import java.util.List;
import lombok.Data;

/** Defines the requested grouping, filtering, and interval for course log statistics. */
@Data
public class CourseLogsStatisticsRequest {

  private Integer courseId;

  private CourseLogsStatisticsRequestTimeRange timeRange;

  private List<CourseLogsGroupBy> groupBy; // Defines the base grouping dimension.

  private TimeInterval interval;
  private Integer weekStartsOn;
  private FillGapStrategy fillGapsStrategy;
  private List<CourseLogsSelectColumn> fields;

  private CourseLogsStatisticsRequestFilters filters;
  private List<SortBy> sortBy;
}
