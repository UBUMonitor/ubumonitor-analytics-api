package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

/**
 * A role a user can hold inside a course.
 *
 * @param id Moodle role identifier
 * @param name display name, falling back to the role short name
 */
@Data
@AllArgsConstructor
public class Role {
  @NonNull Long id;
  String name;
}
