package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.in.rest.mapper;


import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsMetricsRequestDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsMetricsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsMetricsResponseRowComponentDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsMetricsResponseRowDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsMetricsResponseRowEventDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsMetricsResponseRowModuleDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsMetricsResponseRowOriginDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsMetricsResponseRowUserDto;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics.CourseLogsMetricsRequest;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics.CourseLogsMetricsResult;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics.MetricRow;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = GlobalMapperConfig.class)
public interface GetCourseLogsMetricsMapper {

    @Mapping(target = "fillGapsStrategy", source = "courseLogsMetricsRequestDto.fillGapsStrategy", defaultValue = "NONE")
    CourseLogsMetricsRequest toDomain(Integer courseId, CourseLogsMetricsRequestDto courseLogsMetricsRequestDto);

    CourseLogsMetricsResponseDto toDto(CourseLogsMetricsResult result);


    @Mapping(target = "user", source = "metricRow", qualifiedByName = "user")
    @Mapping(target = "module", source = "metricRow", qualifiedByName = "module")
    @Mapping(target = "component", source = "metricRow", qualifiedByName = "component")
    @Mapping(target = "event", source = "metricRow", qualifiedByName = "event")
    @Mapping(target = "origin", source = "metricRow", qualifiedByName = "origin")
    CourseLogsMetricsResponseRowDto toNestedRow(MetricRow metricRow);


    @Named("user")
    default CourseLogsMetricsResponseRowUserDto toUser(MetricRow source) {
        if (source.getUserId() == null && source.getUserFullName() == null) {
            return null;
        }
        return toCourseLogsMetricsResponseRowUserDto(source);
    }
    @Named("module")
    default CourseLogsMetricsResponseRowModuleDto toModule(MetricRow source) {
        if (source.getModuleId() == null && source.getModuleName() == null) {
            return null;
        }
        return toCourseLogsMetricsResponseRowModuleDto(source);
    }
    @Named("component")
    default CourseLogsMetricsResponseRowComponentDto toComponent(MetricRow source) {
        if (source.getComponentId() == null && source.getComponentName() == null) {
            return null;
        }
        return toCourseLogsMetricsResponseRowComponentDto(source);
    }

    @Named("event")
    default CourseLogsMetricsResponseRowEventDto toEvent(MetricRow source) {
        if (source.getEventId() == null && source.getEventName() == null) {
            return null;
        }
        return toCourseLogsMetricsResponseRowEventDto(source);
    }

    @Named("origin")
    default CourseLogsMetricsResponseRowOriginDto toOrigin(MetricRow source) {
        if (source.getOriginId() == null && source.getOriginName() == null) {
            return null;
        }
        return toCourseLogsMetricsResponseRowOriginDto(source);
    }

    @Mapping(target = "id", source = "userId")
    @Mapping(target = "fullName", source = "userFullName")
    CourseLogsMetricsResponseRowUserDto toCourseLogsMetricsResponseRowUserDto(MetricRow source);


    @Mapping(target = "id", source = "moduleId")
    @Mapping(target = "name", source = "moduleName")
    CourseLogsMetricsResponseRowModuleDto toCourseLogsMetricsResponseRowModuleDto(MetricRow source);

    @Mapping(target = "name", source = "componentName")
    @Mapping(target = "id", source = "componentId")
    CourseLogsMetricsResponseRowComponentDto toCourseLogsMetricsResponseRowComponentDto(MetricRow source);

    @Mapping(target = "name", source = "eventName")
    @Mapping(target = "id", source = "eventId")
    CourseLogsMetricsResponseRowEventDto toCourseLogsMetricsResponseRowEventDto(MetricRow source);

    @Mapping(target = "id", source = "originId")
    @Mapping(target = "name", source = "originName")
    CourseLogsMetricsResponseRowOriginDto toCourseLogsMetricsResponseRowOriginDto(MetricRow source);

}
