package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

/**
 * A group of users inside a course.
 *
 * @param id Moodle group identifier
 * @param name group name
 * @param description plain-text group description
 */
@Data
@AllArgsConstructor
public class Group {
  @NonNull Long id;
  String name;
  String description;
}
