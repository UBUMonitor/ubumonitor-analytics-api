package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.MoodleLogCatalog;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.rules.MoodleLogRule;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.config.GrokCompilerConfig;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.config.MoodleRulesConfig;
import java.util.List;
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
                rule((short) 1, (short) 2, "Course viewed", "alternative %{INT:alternative_id}"),
                rule((short) 1, (short) 3, "Course created", null)));

    assertEquals(2, catalog.getEvents().size());
    var courseViewed = catalog.getEvents().get(new MoodleLogCatalog.Key("System", "Course viewed"));
    assertEquals((short) 1, courseViewed.getComponentId());
    assertEquals((short) 2, courseViewed.getEventId());
    assertEquals(3, courseViewed.getGroks().size());
    assertTrue(
        courseViewed.getGroks().stream()
            .anyMatch(grok -> grok.getOriginalGrokPattern().equals("section %{INT:section_id}")));
    assertTrue(
        courseViewed.getGroks().stream()
            .anyMatch(
                grok -> grok.getOriginalGrokPattern().equals("alternative %{INT:alternative_id}")));
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

  @Test
  void matchesForumSubscriptionDescriptionWithExtraWhitespace() {
    MoodleRulesConfig config = new MoodleRulesConfig(new GrokCompilerConfig());
    String description =
        "The user with id '13' subscribed the user with id '13' to the discussion  with id '197' "
            + "in the forum with the course module id '971'.";
    String pattern =
        "The user with id '%{INT:user_id:long}' subscribed the user with id "
            + "'%{INT:target_user_id:long}' to the discussion\\s+with id "
            + "'%{INT:discussion_id:long}' in the forum with the course module id "
            + "'%{INT:course_module_id:long}'.";
    var catalog =
        config.compileRules(
            List.of(
                new MoodleLogRule(
                    (short) 36,
                    "mod_forum",
                    "Forum",
                    (short) 176,
                    "eventdiscussionsubscriptioncreated",
                    "Discussion subscription created",
                    pattern)));

    var entry =
        catalog
            .getEvents()
            .get(new MoodleLogCatalog.Key("Forum", "Discussion subscription created"));
    var match = entry.getGroks().getFirst().match(description);

    assertNotNull(match);
    assertEquals(13L, match.capture().get("user_id"));
    assertEquals(971L, match.capture().get("course_module_id"));
  }

  private MoodleLogRule rule(Short componentId, Short eventId, String eventName, String pattern) {
    return new MoodleLogRule(
        componentId, "core", "System", eventId, "eventcourseviewed", eventName, pattern);
  }
}
