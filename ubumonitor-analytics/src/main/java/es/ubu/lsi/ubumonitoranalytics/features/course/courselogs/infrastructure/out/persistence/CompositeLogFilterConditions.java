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
      if (filter == null || populatedDimensions(filter) < 2) {
        throw new BadRequestException(
            "Each composite filter must contain at least two populated dimensions");
      }

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
      alternatives.add(DSL.and(dimensions));
    }

    return DSL.or(alternatives);
  }

  private static int populatedDimensions(CompositeLogFilter filter) {
    int count = 0;
    if (filter.getUserId() != null) count++;
    if (filter.getModuleId() != null) count++;
    if (filter.getComponentId() != null) count++;
    if (filter.getEventId() != null) count++;
    if (filter.getOriginId() != null) count++;
    if (filter.getSectionId() != null) count++;
    if (filter.getIpAddress() != null) count++;
    return count;
  }
}
