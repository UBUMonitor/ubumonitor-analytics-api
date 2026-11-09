package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.config;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out.MoodleLogCatalog;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.rules.MoodleLogRule;
import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Compiles Moodle log rules loaded from tenant persistence. */
@Component
@RequiredArgsConstructor
public class MoodleRulesConfig {

  private final GrokCompilerConfig grokCompilerConfig;

  public MoodleLogCatalog compileRules(List<MoodleLogRule> rules) {
    GrokCompiler grokCompiler = null; // created lazily: many catalogs have no patterns at all
    Map<MoodleLogCatalog.Key, MoodleLogCatalog.Entry> compiledRules = new LinkedHashMap<>();

    for (MoodleLogRule rule : rules) {
      List<String> patterns = splitPatterns(rule.getGrokPatternEn());
      if (grokCompiler == null && !patterns.isEmpty()) {
        grokCompiler = grokCompilerConfig.createCompiler();
      }

      MoodleLogCatalog.Key key =
          new MoodleLogCatalog.Key(rule.getComponentName(), rule.getEventName());
      MoodleLogCatalog.Entry existing = compiledRules.get(key);

      // Rules sharing a component/event accumulate their patterns, in declaration order.
      List<Grok> groks =
          existing == null ? new ArrayList<>() : new ArrayList<>(existing.getGroks());
      for (String pattern : patterns) {
        groks.add(compile(grokCompiler, rule, pattern));
      }
      compiledRules.put(
          key, new MoodleLogCatalog.Entry(rule.getComponentId(), rule.getEventId(), groks));
    }

    return new MoodleLogCatalog(compiledRules);
  }

  /** One pattern per non-blank line. */
  private static List<String> splitPatterns(String grokPatterns) {
    if (grokPatterns == null || grokPatterns.isBlank()) {
      return List.of();
    }
    List<String> patterns = new ArrayList<>();
    for (String line : grokPatterns.split("\\R")) {
      String pattern = line.trim();
      if (!pattern.isEmpty()) {
        patterns.add(pattern);
      }
    }
    return patterns;
  }

  private static Grok compile(GrokCompiler grokCompiler, MoodleLogRule rule, String pattern) {
    try {
      return grokCompiler.compile(pattern);
    } catch (RuntimeException exception) {
      throw new IllegalStateException(
          "Invalid Grok pattern for component='"
              + rule.getComponentName()
              + "', event='"
              + rule.getEventName()
              + "'",
          exception);
    }
  }
}
