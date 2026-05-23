package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GetCourseLogsCommand {

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Integer courseId;
    private List<Integer> usersIds;
    private List<Integer> modulesIds;
    private List<Byte> componentsIds;
    private List<Short> eventsIds;
    private List<Byte> originsIds;
    private List<String> ipAddresses;
    private List<LogViewColumn> columns;
    private PageableRequest pageableRequest;
    private Boolean includeTotal;
}
