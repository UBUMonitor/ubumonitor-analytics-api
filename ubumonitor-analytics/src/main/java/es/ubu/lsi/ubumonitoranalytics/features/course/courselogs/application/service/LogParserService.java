package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.MoodleLogCatalog;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.ProcessLogLine;
import io.krakens.grok.api.Grok;
import io.krakens.grok.api.Match;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class LogParserService {

  private static final DateTimeFormatter MOODLE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("d/MM/yy, HH:mm:ss");

  public ProcessLogLine processRow(
      Long courseId,
      ZoneId moodleTimeZone,
      CSVRecord row,
      Map<MoodleLogCatalog.Key, MoodleLogCatalog.Entry> logEvents,
      Map<String, Byte> logOrigins) {

    String component = row.get("Component");
    String event = row.get("Event name");
    String origin = row.get("Origin");
    String time = row.get("Time");
    String ip = row.get("IP address");
    String description = row.get("Description");

    MoodleLogCatalog.Entry logEvent = logEvents.get(new MoodleLogCatalog.Key(component, event));
    Byte originId = logOrigins.get(origin);

    if (logEvent == null || originId == null) {
      log.warn(
          "Missing mapping component='{}', event='{}', origin='{}', description='{}'",
          component,
          event,
          origin,
          description);
      return null;
    }

    ProcessLogLine line = new ProcessLogLine();
    line.setTime(
        LocalDateTime.parse(time, MOODLE_TIME_FORMATTER).atZone(moodleTimeZone).toInstant());
    line.setCourseId(courseId);
    line.setComponentId(logEvent.getComponentId());
    line.setEventId(logEvent.getEventId());
    line.setOriginId(originId);
    line.setIpAddress(ip);

    applyAdditionalData(component, event, description, line, logEvent.getGroks());
    return line;
  }

  private void applyAdditionalData(
      String component,
      String eventName,
      String description,
      ProcessLogLine processLogLine,
      List<Grok> groks) {

    if (CollectionUtils.isEmpty(groks)) {
      log.warn(
          "No Grok rules for component='{}', eventName='{}', description='{}'",
          component,
          eventName,
          description);
      return;
    }

    for (Grok grok : groks) {
      if (CollectionUtils.isEmpty(grok.getNamedRegexCollection())
          && grok.getOriginalGrokPattern().equals(description)) {
        return;
      }

      Match match = grok.match(description);
      if (match == null) {
        continue;
      }

      Map<String, Object> capture = match.capture();
      if (capture.isEmpty()) {
        continue;
      }

      applyCapturedData(capture, processLogLine);
      return;
    }

    log.warn(
        "No Grok match found for component='{}', eventName='{}', description='{}'",
        component,
        eventName,
        description);
  }

  private void applyCapturedData(Map<String, Object> grokResult, ProcessLogLine processLogLine) {
    Long userId = (Long) grokResult.get("user_id");
    if (userId != null) {
      processLogLine.setUserId(userId);
    }

    Long moduleId = (Long) grokResult.get("course_module_id");
    if (moduleId != null) {
      processLogLine.setModuleId(moduleId);
    }
  }
}
