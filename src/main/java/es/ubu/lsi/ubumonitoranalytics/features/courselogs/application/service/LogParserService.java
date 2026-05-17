package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.LogLine;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.config.MoodleRulesConfig;
import io.krakens.grok.api.Grok;
import io.krakens.grok.api.Match;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogParserService {
    private static final DateTimeFormatter MOODLE_TIME_FORMATTER = DateTimeFormatter.ofPattern("d/MM/yy, HH:mm:ss");

    private final MoodleRulesConfig moodleRulesConfig;

    public LogLine processRow(Integer courseId, CSVRecord row, Map<String, Byte> componentIds, Map<String, Short> eventIds, Map<String, Byte> logOrigins) {

        String component = row.get("Component");
        String event = row.get("Event name");
        String origin = row.get("Origin");

        Byte componentId = componentIds.get(component);
        Short eventId = eventIds.get(event);
        Byte originId = logOrigins.get(origin);

        if (componentId == null || eventId == null || originId == null) {

            log.warn("Not found in database component='[{}]', eventName='[{}]', origin='[{}]' and description='[{}]'", component, event, origin, row.get("Description"));
            return null;
        }

        LogLine logLine = new LogLine();
        logLine.setTime(LocalDateTime.parse(row.get("Time"), MOODLE_TIME_FORMATTER));
        logLine.setCourseId(courseId);
        logLine.setComponentId(componentId);
        logLine.setEventId(eventId);
        logLine.setOriginId(originId);
        logLine.setIpAddress(row.get("IP address"));
        additionalData(component, event, row.get("Description"), logLine);
        return logLine;
    }

    private void additionalData(String component, String eventName, String description, LogLine logLine) {
        List<Grok> groks = Optional.ofNullable(moodleRulesConfig.getRules().get(component))
            .map(eventMap -> eventMap.get(eventName))
            .orElse(Collections.emptyList());


        for (Grok grok : groks) {
            if (CollectionUtils.isEmpty(grok.getNamedRegexCollection()) && grok.getOriginalGrokPattern().equals(description)) {
                // Si el patrón es exactamente igual a la descripción, no es necesario hacer match
                // Esto es útil para casos donde no se necesitan extraer variables, solo validar la existencia del evento
                return;
            }
            Match match = grok.match(description);
            if (match == null) {
                continue;
            }
            Map<String, Object> capture = match.capture();
            if (!capture.isEmpty()) {
                addAdditionalData(capture, logLine);
                return;
            }
        }

        log.warn("No Grok rule found for component='[{}]', eventName='[{}]', description='{}'", component, eventName, description);

    }

    private void addAdditionalData(Map<String, Object> grokResult, LogLine logLine) {
        Integer userId = (Integer) grokResult.get("user_id");
        logLine.setUserId(userId);

        Integer courseModuleId = (Integer) grokResult.get("course_module_id");
        logLine.setModuleId(courseModuleId);
    }
}
