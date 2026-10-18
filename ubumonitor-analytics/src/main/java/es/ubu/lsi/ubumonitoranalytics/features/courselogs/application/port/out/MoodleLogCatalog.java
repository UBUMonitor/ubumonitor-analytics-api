package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out;

import io.krakens.grok.api.Grok;
import java.util.List;
import java.util.Map;
import lombok.Value;

/** Event lookup entries containing identifiers and compiled parsing rules. */
@Value
public class MoodleLogCatalog {

  Map<Key, Entry> events;

  @Value
  public static class Key {
    String componentName;
    String eventName;
  }

  @Value
  public static class Entry {
    Short componentId;
    Short eventId;
    List<Grok> groks;
  }
}
