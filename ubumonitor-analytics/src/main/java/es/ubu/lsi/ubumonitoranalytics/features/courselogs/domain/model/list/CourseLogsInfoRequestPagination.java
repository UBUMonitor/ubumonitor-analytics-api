package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.list;

import lombok.Data;

@Data
public class CourseLogsInfoRequestPagination {

  private Integer page = 0;
  private Integer size = 20;
}
