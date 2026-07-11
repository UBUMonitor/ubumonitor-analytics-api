package es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public enum TimeInterval {
  HOURLY("yyyy-MM-dd'T'HH:00:00", ChronoUnit.HOURS, false),
  DAILY("yyyy-MM-dd", ChronoUnit.DAYS, true),
  WEEKLY("yyyy-'W'ww", ChronoUnit.WEEKS, true),
  MONTHLY("yyyy-MM", ChronoUnit.MONTHS, true),

  DAY_OF_WEEK(null, null, false);

  private final String pattern;
  private final TemporalUnit unit;
  private final boolean continuous;

  TimeInterval(String pattern, TemporalUnit unit, boolean continuous) {
    this.pattern = pattern;
    this.unit = unit;
    this.continuous = continuous;
  }

  public String pattern() {
    return pattern;
  }

  public TemporalUnit unit() {
    return unit;
  }

  public boolean isContinuous() {
    return continuous;
  }
}
