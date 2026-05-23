package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FetchCourseLogsResult {
    private List<FetchLogLine> logs;
    private Integer page;
    private Integer size;
    private Integer totalElements;
    private Integer totalPages;
    private Boolean first;
    private Boolean last;
    private Boolean empty;

}
