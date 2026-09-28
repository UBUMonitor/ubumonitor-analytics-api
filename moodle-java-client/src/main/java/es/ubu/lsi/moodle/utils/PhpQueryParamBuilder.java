package es.ubu.lsi.moodle.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class PhpQueryParamBuilder {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static Map<String, String> toPhpQuery(Object object) {

    Objects.requireNonNull(object, "Object cannot be null");

    try {
      Map<String, Object> map = MAPPER.convertValue(object, new TypeReference<>() {});

      Map<String, String> result = new LinkedHashMap<>();

      log.debug("Converting object to PHP query parameters: {}", object.getClass().getSimpleName());

      buildQuery(null, map, result);

      log.debug("Successfully converted {} parameters", result.size());

      return result;

    } catch (IllegalArgumentException e) {
      log.error("Failed to convert object to map: {}", e.getMessage(), e);

      throw new IllegalArgumentException("Failed to convert object to PHP query parameters", e);
    }
  }

  private static void buildQuery(String prefix, Object value, Map<String, String> params) {

    switch (value) {
      case null -> log.trace("Skipping null value at prefix: {}", prefix);

      case Map<?, ?> map -> {
        for (Map.Entry<?, ?> entry : map.entrySet()) {

          String key =
              prefix == null ? entry.getKey().toString() : prefix + "[" + entry.getKey() + "]";

          buildQuery(key, entry.getValue(), params);
        }
      }

      case List<?> list -> {
        for (int i = 0; i < list.size(); i++) {
          buildQuery(prefix + "[" + i + "]", list.get(i), params);
        }
      }

      default -> {
        params.put(prefix, String.valueOf(value));

        log.trace("Added parameter: {} = {}", prefix, value);
      }
    }
  }

  public static String ofFormData(Map<String, String> data) {

    return data.entrySet().stream()
        .map(
            e ->
                URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                    + "="
                    + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
        .collect(Collectors.joining("&"));
  }
}
