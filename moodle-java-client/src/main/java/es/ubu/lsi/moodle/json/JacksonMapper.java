package es.ubu.lsi.moodle.json;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.ubu.lsi.moodle.exception.JsonMappingException;
import es.ubu.lsi.moodle.model.ajax.AjaxResponse;
import java.util.List;

/** Maps Java values to and from Moodle JSON payloads. */
public class JacksonMapper {

  private final ObjectMapper mapper = new ObjectMapper();

  /**
   * @param obj value to serialize
   * @return JSON representation
   * @throws JsonMappingException when serialization fails
   */
  public String toJson(Object obj) {
    try {
      return mapper.writeValueAsString(obj);
    } catch (Exception e) {
      throw new JsonMappingException("Error serializing object to JSON", e);
    }
  }

  /**
   * @param json JSON document
   * @param type target model class
   * @param <T> target model type
   * @return mapped model
   * @throws JsonMappingException when deserialization fails
   */
  public <T> T fromJson(String json, Class<T> type) {
    try {
      return mapper.readValue(json, type);
    } catch (Exception e) {
      throw new JsonMappingException("Error deserializing JSON to " + type.getSimpleName(), e);
    }
  }

  /**
   * @param json JSON document
   * @return parsed JSON tree
   * @throws JsonMappingException when parsing fails
   */
  public JsonNode readTree(String json) {
    try {
      return mapper.readTree(json);
    } catch (Exception e) {
      throw new JsonMappingException("Error parsing JSON tree", e);
    }
  }

  /**
   * @param json JSON array
   * @param type target element class
   * @param <T> target element type
   * @return mapped elements
   * @throws JsonMappingException when deserialization fails
   */
  public <T> List<T> fromJsonArray(String json, Class<T> type) {
    try {
      JavaType javaType = mapper.getTypeFactory().constructCollectionType(List.class, type);

      return mapper.readValue(json, javaType);
    } catch (Exception e) {
      throw new JsonMappingException(
          "Error deserializing JSON array to List<" + type.getSimpleName() + ">", e);
    }
  }

  /**
   * @param json AJAX response array
   * @param dataType target response data class
   * @param <T> target response data type
   * @return mapped AJAX response envelopes
   * @throws JsonMappingException when deserialization fails
   */
  public <T> List<AjaxResponse<T>> fromJsonAjaxArray(String json, Class<T> dataType) {
    try {
      JavaType responseType =
          mapper.getTypeFactory().constructParametricType(AjaxResponse.class, dataType);
      JavaType javaType = mapper.getTypeFactory().constructCollectionType(List.class, responseType);

      return mapper.readValue(json, javaType);
    } catch (Exception e) {
      throw new JsonMappingException(
          "Error deserializing JSON array to List<AjaxResponse<" + dataType.getSimpleName() + ">>",
          e);
    }
  }
}
