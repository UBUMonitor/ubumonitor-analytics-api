package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.Data;

/**
 * The enrollments of a single course. This is the only source of truth: the course, group and role
 * catalogs are derived from the enrollments and can never get out of sync with them.
 *
 * @param courseId Moodle identifier of the course the enrollments belong to
 * @param enrollments one entry per enrolled user
 */
@Data
public class CourseEnrollments {
  Long courseId;
  List<Enrollment> enrollments;

  public CourseEnrollments(Long courseId, List<Enrollment> enrollments) {
    this.courseId = Objects.requireNonNull(courseId, "courseId");
    this.enrollments = enrollments == null ? List.of() : List.copyOf(enrollments);
  }

  /**
   * @param courseId Moodle course identifier
   * @return enrollments with no users
   */
  public static CourseEnrollments empty(Long courseId) {
    return new CourseEnrollments(courseId, List.of());
  }

  /**
   * @return the enrolled users
   */
  public List<User> users() {
    return enrollments.stream().map(Enrollment::getUser).toList();
  }

  /**
   * @return identifiers of the enrolled users
   */
  public List<Long> userIds() {
    return enrollments.stream().map(enrollment -> enrollment.getUser().getId()).toList();
  }

  /**
   * @return every distinct course any enrolled user belongs to
   */
  public List<Course> courses() {
    return distinctById(
        enrollments.stream()
            .flatMap(enrollment -> enrollment.getCourses().stream())
            .map(UserCourse::getCourse),
        Course::getId);
  }

  /**
   * @return every distinct group any enrolled user belongs to
   */
  public List<Group> groups() {
    return distinctById(
        enrollments.stream().flatMap(enrollment -> enrollment.getGroups().stream()), Group::getId);
  }

  /**
   * @return every distinct role any enrolled user holds
   */
  public List<Role> roles() {
    return distinctById(
        enrollments.stream().flatMap(enrollment -> enrollment.getRoles().stream()), Role::getId);
  }

  private static <T> List<T> distinctById(Stream<T> items, Function<T, Long> idOf) {
    Map<Long, T> byId = new LinkedHashMap<>();
    items.forEach(item -> byId.putIfAbsent(idOf.apply(item), item));
    return List.copyOf(byId.values());
  }
}
