package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.MoodleLogCatalog;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.rules.MoodleLogRule;
import java.util.List;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.config.GrokCompilerConfig;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.config.MoodleRulesConfig;
import org.junit.jupiter.api.Test;

class MoodleRulesConfigTest {

  @Test
  void compilesRulesUsingUnbracketedComponentAndEventNames() {
    MoodleRulesConfig config = new MoodleRulesConfig(new GrokCompilerConfig());

    var catalog =
        config.compileRules(
            List.of(
                rule(
                    (short) 1,
                    (short) 2,
                    "Course viewed",
                    "course %{INT:course_id}\nsection %{INT:section_id}"),
                rule((short) 1, (short) 3, "Course created", null)));

    assertEquals(2, catalog.getEvents().size());
    var courseViewed = catalog.getEvents().get(new MoodleLogCatalog.Key("System", "Course viewed"));
    assertEquals((short) 1, courseViewed.getComponentId());
    assertEquals((short) 2, courseViewed.getEventId());
    assertEquals(2, courseViewed.getGroks().size());
    assertTrue(
        courseViewed.getGroks().stream()
            .anyMatch(grok -> grok.getOriginalGrokPattern().equals("section %{INT:section_id}")));
    assertEquals(
        (short) 3,
        catalog.getEvents().get(new MoodleLogCatalog.Key("System", "Course created")).getEventId());
    assertTrue(
        catalog
            .getEvents()
            .get(new MoodleLogCatalog.Key("System", "Course created"))
            .getGroks()
            .isEmpty());
  }

  private MoodleLogRule rule(Short componentId, Short eventId, String eventName, String pattern) {
    return new MoodleLogRule(
        componentId, "core", "System", eventId, "eventcourseviewed", eventName, pattern);
  }
}
