package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics;

import java.util.List;
import lombok.Data;

/** Defines the requested grouping, filtering, and interval for course log statistics. */
@Data
public class CourseLogsStatisticsRequest {

  private Long courseId;

  private CourseLogsStatisticsRequestTimeRange timeRange;

  private TimeInterval interval;
  private String timeZone;
  private List<CourseLogsSelectColumn> fields;

  private CourseLogsStatisticsRequestFilters filters;
}
