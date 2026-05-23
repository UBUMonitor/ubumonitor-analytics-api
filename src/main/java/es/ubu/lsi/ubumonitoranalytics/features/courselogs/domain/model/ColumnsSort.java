package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ColumnsSort {
    private LogViewColumn column;
    private Direction direction;
}
