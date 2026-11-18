package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Logs.LOGS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Modules.MODULES;

import java.util.Collection;
import java.util.List;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

/** WHERE building blocks shared by every query over the logs table. */
final class LogConditions {

  private LogConditions() {}

  /** {@code from} and {@code to} are inclusive UTC epoch seconds; either may be null. */
  static Condition timeRange(Long from, Long to) {
    Condition condition = DSL.noCondition();
    if (from != null) {
      condition = condition.and(LOGS.LOGGED_AT.greaterOrEqual(from));
    }
    if (to != null) {
      condition = condition.and(LOGS.LOGGED_AT.lessOrEqual(to));
    }
    return condition;
  }

  /** A null collection means "no filter"; an empty one matches nothing. */
  static <T> Condition in(Field<T> field, Collection<T> values) {
    if (values == null) {
      return DSL.noCondition();
    }
    return values.isEmpty() ? DSL.falseCondition() : field.in(values);
  }

  /** Logs of any module that belongs to one of the sections. Same null/empty rules as in. */
  static Condition inSections(Collection<Long> sectionIds) {
    if (sectionIds == null) {
      return DSL.noCondition();
    }
    if (sectionIds.isEmpty()) {
      return DSL.falseCondition();
    }
    return LOGS.MODULE_ID.in(
        DSL.select(MODULES.ID).from(MODULES).where(MODULES.SECTION_ID.in(sectionIds)));
  }

  /** Conditions shared by log listing and statistics, AND-combined. */
  static Condition filters(
      List<Long> userIds,
      List<Long> moduleIds,
      List<Short> componentIds,
      List<Short> eventIds,
      List<Byte> originIds,
      List<String> ipAddresses,
      List<Long> sectionIds) {
    return DSL.and(
        in(LOGS.USER_ID, userIds),
        in(LOGS.MODULE_ID, moduleIds),
        in(LOGS.COMPONENT_ID, componentIds),
        in(LOGS.EVENT_ID, eventIds),
        in(LOGS.ORIGIN_ID, originIds),
        in(LOGS.IP_ADDRESS, ipAddresses),
        inSections(sectionIds));
  }
}
