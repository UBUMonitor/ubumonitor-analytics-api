package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info;

import lombok.Data;

import java.util.List;

@Data
public class CourseLogsInfoRequestFilters {

    private List<Integer> userIds;
    private List<Integer> moduleIds;
    private List<Byte> componentIds;
    private List<Short> eventIds;
    private List<Byte> originIds;
    private List<Integer> sectionIds;
    private List<String> ipAddresses;
}
