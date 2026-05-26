package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics;

import lombok.Data;

@Data
public class SortBy {

    private SortField field;
    private SortDirection direction;

    public enum SortField {
        VALUE,
        USER_ID,
        MODULE_ID,
        TIME,
        TIME_BUCKET
    }

    public enum SortDirection {
        ASC,
        DESC
    }
}
