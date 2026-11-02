package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

/**
 * A course a user is enrolled in.
 *
 * @param course the course
 * @param lastCourseAccess epoch seconds of the user's last access to that course, or {@code null}
 *     when unknown
 */
@Data
@AllArgsConstructor
public class UserCourse {
  @NonNull Course course;
  Long lastCourseAccess;
}
