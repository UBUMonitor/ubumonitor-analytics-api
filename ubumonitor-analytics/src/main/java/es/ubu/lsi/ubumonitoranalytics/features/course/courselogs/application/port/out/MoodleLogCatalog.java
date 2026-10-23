package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.out;

import io.krakens.grok.api.Grok;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

/** Event lookup entries containing identifiers and compiled parsing rules. */
@Data
@AllArgsConstructor
public class MoodleLogCatalog {

  Map<Key, Entry> events;

  @Data
  @AllArgsConstructor
  public static class Key {
    String componentName;
    String eventName;
  }

  @Data
  @AllArgsConstructor
  public static class Entry {
    Short componentId;
    Short eventId;
    List<Grok> groks;
  }
}
