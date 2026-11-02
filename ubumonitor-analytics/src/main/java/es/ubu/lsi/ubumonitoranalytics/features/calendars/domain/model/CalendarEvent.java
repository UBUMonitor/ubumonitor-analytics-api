package es.ubu.lsi.ubumonitoranalytics.features.calendars.domain.model;

public record CalendarEvent(
    Long id,
    String name,
    String description,
    Long courseId,
    Long categoryId,
    Long groupId,
    Long userId,
    Long repeatId,
    String moduleName,
    Long instance,
    String eventType,
    Long timeStart,
    Long timeDuration,
    Integer visible,
    Integer sequence,
    Long timeModified,
    Long subscriptionId,
    String uuid) {}
