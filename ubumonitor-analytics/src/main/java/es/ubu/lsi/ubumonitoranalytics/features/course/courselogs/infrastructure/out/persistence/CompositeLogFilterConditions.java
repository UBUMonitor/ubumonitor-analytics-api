package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Logs.LOGS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Modules.MODULES;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.CompositeLogFilter;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.BadRequestException;
import java.util.ArrayList;
import java.util.List;
import org.jooq.Condition;
import org.jooq.impl.DSL;

/** Builds validated OR-of-AND criteria for compound course log filtering. */
final class CompositeLogFilterConditions {

  private CompositeLogFilterConditions() {}

  static Condition build(List<CompositeLogFilter> filters) {
    if (filters == null || filters.isEmpty()) {
      return DSL.noCondition();
    }

    List<Condition> alternatives = new ArrayList<>();
    for (CompositeLogFilter filter : filters) {
      List<Condition> dimensions = filter == null ? List.of() : dimensionsOf(filter);
      if (dimensions.size() < 2) {
        throw new BadRequestException(
            "Each composite filter must contain at least two populated dimensions");
      }
      alternatives.add(DSL.and(dimensions));
    }

    return DSL.or(alternatives);
  }

  /** One equality condition per populated field of the filter. */
  private static List<Condition> dimensionsOf(CompositeLogFilter filter) {
    List<Condition> dimensions = new ArrayList<>();
    if (filter.getUserId() != null) {
      dimensions.add(LOGS.USER_ID.eq(filter.getUserId()));
    }
    if (filter.getModuleId() != null) {
      dimensions.add(LOGS.MODULE_ID.eq(filter.getModuleId()));
    }
    if (filter.getComponentId() != null) {
      dimensions.add(LOGS.COMPONENT_ID.eq(filter.getComponentId()));
    }
    if (filter.getEventId() != null) {
      dimensions.add(LOGS.EVENT_ID.eq(filter.getEventId()));
    }
    if (filter.getOriginId() != null) {
      dimensions.add(LOGS.ORIGIN_ID.eq(filter.getOriginId()));
    }
    if (filter.getSectionId() != null) {
      dimensions.add(
          LOGS.MODULE_ID.in(
              DSL.select(MODULES.ID)
                  .from(MODULES)
                  .where(MODULES.SECTION_ID.eq(filter.getSectionId()))));
    }
    if (filter.getIpAddress() != null) {
      dimensions.add(LOGS.IP_ADDRESS.eq(filter.getIpAddress()));
    }
    return dimensions;
  }
}
