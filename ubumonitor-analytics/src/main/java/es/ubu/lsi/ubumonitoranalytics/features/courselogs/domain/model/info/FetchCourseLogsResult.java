package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info;

import java.util.List;
import lombok.Builder;
import lombok.Data;

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
