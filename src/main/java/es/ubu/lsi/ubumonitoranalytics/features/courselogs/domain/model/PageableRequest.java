package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class PageableRequest {
    private Integer page;
    private Integer size;
    private List<ColumnsSort> sort;
}
