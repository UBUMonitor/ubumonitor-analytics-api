package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.ProcessLogLine;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class LogParserService {

    private static final DateTimeFormatter MOODLE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("d/MM/yy, HH:mm:ss");

    private final MoodleRulesConfig moodleRulesConfig;


    private final Map<String, List<Grok>> grokCache = new HashMap<>();

    public LogParserService(MoodleRulesConfig moodleRulesConfig) {
        this.moodleRulesConfig = moodleRulesConfig;
        for (Map.Entry<String, Map<String, List<Grok>>> componentEntry
            : moodleRulesConfig.getRules().entrySet()) {

            String component = componentEntry.getKey();

            Map<String, List<Grok>> eventMap = componentEntry.getValue();
            if (eventMap == null) continue;

            for (Map.Entry<String, List<Grok>> eventEntry : eventMap.entrySet()) {

                String event = eventEntry.getKey();
                List<Grok> groks = eventEntry.getValue();

                grokCache.put(component + "::" + event,
                    groks != null ? groks : Collections.emptyList()
                );
            }
        }
    }

    public ProcessLogLine processRow(
        Integer courseId,
        CSVRecord row,
        Map<String, Byte> componentIds,
        Map<String, Short> eventIds,
        Map<String, Byte> logOrigins
    ) {

        // ✅ Cache local de columnas (evita repetidos row.get)
        String component = row.get("Component");
        String event = row.get("Event name");
        String origin = row.get("Origin");
        String time = row.get("Time");
        String ip = row.get("IP address");
        String description = row.get("Description");

        // ✅ Map lookups (sin Optional, sin overhead extra)
        Byte componentId = componentIds.get(component);
        Short eventId = eventIds.get(event);
        Byte originId = logOrigins.get(origin);

        if (componentId == null || eventId == null || originId == null) {
            log.warn(
                "Missing mapping component='{}', event='{}', origin='{}'",
                component, event, origin
            );
            return null;
        }

        ProcessLogLine line = new ProcessLogLine();

        // ⚡ parse directo
        line.setTime(LocalDateTime.parse(time, MOODLE_TIME_FORMATTER));
        line.setCourseId(courseId);
        line.setComponentId(componentId);
        line.setEventId(eventId);
        line.setOriginId(originId);
        line.setIpAddress(ip);

        applyAdditionalData(component, event, description, line);

        return line;
    }

    private void applyAdditionalData(
        String component,
        String eventName,
        String description,
        ProcessLogLine processLogLine
    ) {

        String key = component + "::" + eventName;

        List<Grok> groks = grokCache.computeIfAbsent(key, k -> {
            Map<String, List<Grok>> eventMap =
                moodleRulesConfig.getRules().get(component);

            if (eventMap == null) {
                return Collections.emptyList();
            }

            List<Grok> list = eventMap.get(eventName);
            return list != null ? list : Collections.emptyList();
        });

        if (groks.isEmpty()) {
            log.warn(
                "No Grok rules for component='{}', eventName='{}'",
                component, eventName
            );
            return;
        }

        for (Grok grok : groks) {

            // ⚡ micro-opt: evita match si no hay necesidad
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
            "No Grok match found for component='{}', eventName='{}'",
            component, eventName
        );
    }

    private void applyCapturedData(
        Map<String, Object> grokResult,
        ProcessLogLine processLogLine
    ) {

        Object userId = grokResult.get("user_id");
        if (userId != null) {
            processLogLine.setUserId((Integer) userId);
        }

        Object moduleId = grokResult.get("course_module_id");
        if (moduleId != null) {
            processLogLine.setModuleId((Integer) moduleId);
        }
    }
}
