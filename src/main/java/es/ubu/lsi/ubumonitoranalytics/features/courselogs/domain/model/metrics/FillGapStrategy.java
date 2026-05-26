package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FillGapStrategy {
    ZERO(0),
    NULL(null),
    NONE(null);

    private final Integer value;

}
