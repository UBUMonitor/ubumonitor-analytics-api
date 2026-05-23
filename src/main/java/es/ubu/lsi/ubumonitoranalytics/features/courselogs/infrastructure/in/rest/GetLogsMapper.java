package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.GetCourseLogsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.LogEntryDto;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.Direction;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.ColumnsSort;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.FetchCourseLogsResult;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.FetchLogLine;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.GetCourseLogsCommand;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.LogViewColumn;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Objects;

@Mapper(config = GlobalMapperConfig.class)
public interface GetLogsMapper {



    @Mapping(target = "pageableRequest.size", source = "size", defaultValue = "50")
    @Mapping(target = "pageableRequest.page", source = "page", defaultValue = "0")
    @Mapping(target = "pageableRequest.sort", source = "sort")
    GetCourseLogsCommand toDomain(String courseId, String startDateTime, String endDateTime, List<String> columns, List<Integer> usersIds, List<Integer> modulesIds, List<Integer> componentsIds, List<Integer> eventsIds, List<Integer> originsIds, List<String> ipAddresses, Integer page, Integer size, List<String> sort, Boolean includeTotal);

    default List<ColumnsSort> sort(List<String> sorts) {

        if (sorts == null || sorts.isEmpty()) {
            return List.of();
        }
        return sorts.stream()
            .map(this::getColumnsSort)
            .filter(Objects::nonNull)
            .toList();

    }

    @Named("columnSort")
    default ColumnsSort  getColumnsSort(String sort) {
        if (sort == null || sort.isEmpty()) {
            return null;
        }
        String [] values = sort.split(":");
        return ColumnsSort.builder()
            .column(LogViewColumn.valueOf(values[0]))
            .direction("DESC".equals(values[0]) ? Direction.DESC : Direction.ASC)
            .build();
    }

    @Mapping(target = "content", source = "logs")
    @Mapping(target = "page", source = "fetchCourseLogsResult")
    GetCourseLogsResponseDto toDto(FetchCourseLogsResult fetchCourseLogsResult);

    @Mapping(target = "timeCreated", source = "timestamp")
    @Mapping(target = "ip", source = "ipAddress")
    @Mapping(target = "component", source = "componentName")
    LogEntryDto toLogEntryDto(FetchLogLine fetchLogLine);
}
