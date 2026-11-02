package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class CourseEnrollmentsTest {

  @Test
  void derivesDistinctCatalogsFromEnrollments() {
    Course requestedCourse = new Course(2L, "Requested", "REQ", null);
    Course otherCourse = new Course(3L, "Other", "OTH", null);
    Group group = new Group(7L, "Group", null);
    Role role = new Role(5L, "Student");
    Enrollment firstEnrollment =
        new Enrollment(
            user(13L),
            List.of(role),
            List.of(group),
            List.of(new UserCourse(requestedCourse, 100L), new UserCourse(otherCourse, 200L)));
    Enrollment secondEnrollment =
        new Enrollment(
            user(14L),
            List.of(new Role(5L, "Student")),
            List.of(new Group(7L, "Group", null)),
            List.of(new UserCourse(requestedCourse, 300L)));

    CourseEnrollments enrollments =
        new CourseEnrollments(2L, List.of(firstEnrollment, secondEnrollment));

    assertEquals(List.of(requestedCourse, otherCourse), enrollments.courses());
    assertEquals(List.of(group), enrollments.groups());
    assertEquals(List.of(role), enrollments.roles());
    assertEquals(List.of(13L, 14L), enrollments.userIds());
  }

  private User user(Long id) {
    return new User(id, "User " + id, "User", id.toString(), null, null, null, null);
  }
}
