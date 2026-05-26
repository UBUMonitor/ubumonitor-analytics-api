package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info;

import lombok.Data;

@Data
public class CourseLogsInfoRequestSortItem {

    private LogViewColumn field;
    private Direction direction;

    public enum Direction {
        ASC, DESC
    }
}
