package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.list;

import lombok.Data;

@Data
public class CourseLogsInfoRequestSortItem {

  private LogViewColumn field;
  private Direction direction;

  public enum Direction {
    ASC,
    DESC
  }
}
