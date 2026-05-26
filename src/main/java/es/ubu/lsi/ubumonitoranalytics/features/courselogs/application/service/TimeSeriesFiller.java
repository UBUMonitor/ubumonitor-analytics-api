package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics.CourseLogsMetricsRequest;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics.FillGapStrategy;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics.MetricRow;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics.TimeInterval;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TimeSeriesFiller {

    public List<MetricRow> fill(
        List<MetricRow> rows,
        CourseLogsMetricsRequest request
    ) {
        if (request.getInterval() == null || request.getTimeRange() == null ||
            !request.getInterval().isContinuous() || request.getFillGapsStrategy() == FillGapStrategy.NONE) {
            return rows;
        }

        Map<String, MetricRow> indexed = rows.stream()
            .collect(Collectors.toMap(
                MetricRow::getTimeBucket,
                r -> r,
                (a, b) -> a
            ));

        List<String> fullRange = generateTimeBuckets(
            request.getTimeRange().getFrom(),
            request.getTimeRange().getTo(),
            request.getInterval()
        );

        List<MetricRow> result = new ArrayList<>();
        Integer value = request.getFillGapsStrategy().getValue();
        for (String bucket : fullRange) {
            MetricRow existing = indexed.get(bucket);

            if (existing != null) {
                result.add(existing);
            } else {
                result.add(MetricRow.builder()
                    .timeBucket(bucket)
                    .value(value)
                    .build());
            }
        }

        return result;
    }

    private List<String> generateTimeBuckets(
        LocalDateTime from,
        LocalDateTime to,
        TimeInterval interval
    ) {
        List<String> buckets = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(interval.pattern());

        TemporalUnit unit = interval.unit();

        LocalDateTime cursor = from;

        while (!cursor.isAfter(to)) {
            buckets.add(cursor.format(formatter));
            cursor = cursor.plus(1, unit);
        }

        return buckets;
    }


}
