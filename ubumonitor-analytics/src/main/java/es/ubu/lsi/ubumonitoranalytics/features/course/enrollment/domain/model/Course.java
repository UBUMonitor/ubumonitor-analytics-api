package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

/**
 * A Moodle course known to the platform.
 *
 * @param id Moodle course identifier
 * @param fullName full course name
 * @param shortName short course name
 * @param lastSync epoch seconds of the last enrollment synchronization, or {@code null} if the
 *     course has never been synchronized
 */
@Data
@AllArgsConstructor
public class Course {
  @NonNull Long id;
  String fullName;
  String shortName;
  Long lastSync;
}
