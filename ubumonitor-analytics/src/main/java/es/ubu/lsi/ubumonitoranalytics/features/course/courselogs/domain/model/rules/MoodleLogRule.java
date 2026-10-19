package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.rules;

import lombok.AllArgsConstructor;
import lombok.Data;

/** Describes one Grok pattern for a Moodle component and event. */
@Data
@AllArgsConstructor
public class MoodleLogRule {
  Short componentId;
  String componentCode;
  String componentName;
  Short eventId;
  String eventCode;
  String eventName;
  String grokPatternEn;
}
