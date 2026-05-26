package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info;

import lombok.Data;

import java.util.List;

@Data
public class CourseLogsInfoRequest {
    private Integer courseId;
    private CourseLogsInfoRequestTimeRange timeRange;
    private CourseLogsInfoRequestFilters filters;
    private List<LogViewColumn> fields;
    private CourseLogsInfoRequestPagination pagination;
    private List<CourseLogsInfoRequestSortItem> sort;
    private Boolean includeTotal;
}
