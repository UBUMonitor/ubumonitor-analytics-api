package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics;

import lombok.Data;

import java.util.List;

@Data
public class CourseLogsMetricsRequestFilters {

    private List<Integer> userIds;
    private List<Integer> moduleIds;
    private List<Byte> componentIds;
    private List<Short> eventIds;
    private List<Byte> originIds;
    private List<String> ipAddresses;
    private List<Integer> sectionIds;
}
