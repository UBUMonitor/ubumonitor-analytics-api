package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model;

import java.util.List;
import java.util.Objects;
import lombok.Data;

/**
 * A user together with their course-scoped data: the roles and groups held in the course being
 * synchronized, and every course the user is enrolled in.
 *
 * @param user the enrolled user
 * @param roles roles held in the enrolled course
 * @param groups groups the user belongs to in the enrolled course
 * @param courses all courses the user is enrolled in, including the enrolled course
 */
@Data
public class Enrollment {
  User user;
  List<Role> roles;
  List<Group> groups;
  List<UserCourse> courses;

  public Enrollment(User user, List<Role> roles, List<Group> groups, List<UserCourse> courses) {
    this.user = Objects.requireNonNull(user, "user");
    this.roles = roles == null ? List.of() : List.copyOf(roles);
    this.groups = groups == null ? List.of() : List.copyOf(groups);
    this.courses = courses == null ? List.of() : List.copyOf(courses);
  }

  /**
   * @param courseId Moodle course identifier
   * @return epoch seconds of the user's last access to the course, or {@code null} when the user is
   *     not enrolled in it or the access is unknown
   */
  public Long lastAccessTo(Long courseId) {
    return courses.stream()
        .filter(userCourse -> userCourse.getCourse().getId().equals(courseId))
        .map(UserCourse::getLastCourseAccess)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }
}
