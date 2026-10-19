package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.in.rest.mapper;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsStatisticsRequestDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsStatisticsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsStatisticsResponseRowComponentDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsStatisticsResponseRowDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsStatisticsResponseRowEventDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsStatisticsResponseRowModuleDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsStatisticsResponseRowOriginDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsStatisticsResponseRowUserDto;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.CourseLogsStatisticsRequest;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.CourseLogsStatisticsResult;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.StatisticRow;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/** Maps course log statistic requests between REST and application models. */
@Mapper(config = GlobalMapperConfig.class)
public interface GetCourseLogsStatisticsMapper {

  @Mapping(
      target = "fillGapsStrategy",
      source = "courseLogsStatisticsRequestDto.fillGapsStrategy",
      defaultValue = "NONE")
  @Mapping(
      target = "weekStartsOn",
      source = "courseLogsStatisticsRequestDto.weekStartsOn",
      defaultValue = "1")
  CourseLogsStatisticsRequest toDomain(
      Integer courseId, CourseLogsStatisticsRequestDto courseLogsStatisticsRequestDto);

  CourseLogsStatisticsResponseDto toDto(CourseLogsStatisticsResult result);

  @Mapping(target = "user", source = "statisticRow", qualifiedByName = "user")
  @Mapping(target = "module", source = "statisticRow", qualifiedByName = "module")
  @Mapping(target = "component", source = "statisticRow", qualifiedByName = "component")
  @Mapping(target = "event", source = "statisticRow", qualifiedByName = "event")
  @Mapping(target = "origin", source = "statisticRow", qualifiedByName = "origin")
  CourseLogsStatisticsResponseRowDto toNestedRow(StatisticRow statisticRow);

  @Named("user")
  default CourseLogsStatisticsResponseRowUserDto toUser(StatisticRow source) {
    if (source.getUserId() == null && source.getUserFullName() == null) {
      return null;
    }
    return toCourseLogsStatisticsResponseRowUserDto(source);
  }

  @Named("module")
  default CourseLogsStatisticsResponseRowModuleDto toModule(StatisticRow source) {
    if (source.getModuleId() == null && source.getModuleName() == null) {
      return null;
    }
    return toCourseLogsStatisticsResponseRowModuleDto(source);
  }

  @Named("component")
  default CourseLogsStatisticsResponseRowComponentDto toComponent(StatisticRow source) {
    if (source.getComponentId() == null && source.getComponentName() == null) {
      return null;
    }
    return toCourseLogsStatisticsResponseRowComponentDto(source);
  }

  @Named("event")
  default CourseLogsStatisticsResponseRowEventDto toEvent(StatisticRow source) {
    if (source.getEventId() == null && source.getEventName() == null) {
      return null;
    }
    return toCourseLogsStatisticsResponseRowEventDto(source);
  }

  @Named("origin")
  default CourseLogsStatisticsResponseRowOriginDto toOrigin(StatisticRow source) {
    if (source.getOriginId() == null && source.getOriginName() == null) {
      return null;
    }
    return toCourseLogsStatisticsResponseRowOriginDto(source);
  }

  @Mapping(target = "id", source = "userId")
  @Mapping(target = "fullName", source = "userFullName")
  CourseLogsStatisticsResponseRowUserDto toCourseLogsStatisticsResponseRowUserDto(
      StatisticRow source);

  @Mapping(target = "id", source = "moduleId")
  @Mapping(target = "name", source = "moduleName")
  CourseLogsStatisticsResponseRowModuleDto toCourseLogsStatisticsResponseRowModuleDto(
      StatisticRow source);

  @Mapping(target = "name", source = "componentName")
  @Mapping(target = "id", source = "componentId")
  CourseLogsStatisticsResponseRowComponentDto toCourseLogsStatisticsResponseRowComponentDto(
      StatisticRow source);

  @Mapping(target = "name", source = "eventName")
  @Mapping(target = "id", source = "eventId")
  CourseLogsStatisticsResponseRowEventDto toCourseLogsStatisticsResponseRowEventDto(
      StatisticRow source);

  @Mapping(target = "id", source = "originId")
  @Mapping(target = "name", source = "originName")
  CourseLogsStatisticsResponseRowOriginDto toCourseLogsStatisticsResponseRowOriginDto(
      StatisticRow source);
}
