package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.UserPicture;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

/**
 * A platform user. Course-scoped data (roles, groups, courses) lives in {@link Enrollment}.
 *
 * @param id Moodle user identifier
 * @param fullName full name
 * @param firstName first name
 * @param lastName last name
 * @param email email address
 * @param firstAccess epoch seconds of the first access to the platform, if any
 * @param lastAccess epoch seconds of the last access to the platform, if any
 * @param userPicture profile picture, or {@code null} when not loaded
 */
@Data
@AllArgsConstructor
public class User {
  @NonNull Long id;
  String fullName;
  String firstName;
  String lastName;
  String email;
  Long firstAccess;
  Long lastAccess;
  UserPicture userPicture;
}
