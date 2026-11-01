package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics;

import java.util.List;
import lombok.Data;

/** Defines the requested grouping, filtering, and interval for course log statistics. */
@Data
public class CourseLogsStatisticsRequest {

  private Long courseId;

  private CourseLogsStatisticsRequestTimeRange timeRange;

  private List<CourseLogsGroupBy> groupBy; // Defines the base grouping dimension.

  private TimeInterval interval;
  private List<CourseLogsSelectColumn> fields;

  private CourseLogsStatisticsRequestFilters filters;
  private List<SortBy> sortBy;
}
