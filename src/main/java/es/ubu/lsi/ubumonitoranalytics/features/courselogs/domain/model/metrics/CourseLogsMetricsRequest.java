package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics;

import lombok.Data;

import java.util.List;

@Data
public class CourseLogsMetricsRequest {

    private Integer courseId;

    private CourseLogsMetricsRequestTimeRange timeRange;

    private List<CourseLogsGroupBy> groupBy; // Define la dimensión base de agrupación

    private TimeInterval interval;
    private FillGapStrategy fillGapsStrategy;
    private List<CourseLogsSelectColumn> fields;


    private CourseLogsMetricsRequestFilters filters;
    private List<SortBy> sortBy;
}
