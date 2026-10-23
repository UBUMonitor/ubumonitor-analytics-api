package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.MoodleLogCatalog;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogContext {
  private Long courseId;
  private Instant lastLogTime;
  private ZoneId moodleTimeZone;
  private Map<String, Byte> logOrigins;
  private MoodleLogCatalog moodleLogCatalog;
}
