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
    GrokCompiler grokCompiler = null;
    Map<MoodleLogCatalog.Key, MoodleLogCatalog.Entry> compiledRules = new LinkedHashMap<>();

    for (MoodleLogRule rule : rules) {
      List<Grok> groks = new ArrayList<>();
      if (rule.getGrokPatternEn() != null && !rule.getGrokPatternEn().isBlank()) {
        for (String candidate : rule.getGrokPatternEn().split("\\R")) {
          String pattern = candidate.trim();
          if (pattern.isEmpty()) {
            continue;
          }

          if (grokCompiler == null) {
            grokCompiler = grokCompilerConfig.createCompiler();
          }

          try {
            groks.add(grokCompiler.compile(pattern));
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
      MoodleLogCatalog.Key key =
          new MoodleLogCatalog.Key(rule.getComponentName(), rule.getEventName());
      MoodleLogCatalog.Entry existing = compiledRules.get(key);
      if (existing != null) {
        List<Grok> combinedGroks = new ArrayList<>(existing.getGroks());
        combinedGroks.addAll(groks);
        groks = combinedGroks;
      }
      compiledRules.put(
          key, new MoodleLogCatalog.Entry(rule.getComponentId(), rule.getEventId(), groks));
    }

    return new MoodleLogCatalog(compiledRules);
  }
}
