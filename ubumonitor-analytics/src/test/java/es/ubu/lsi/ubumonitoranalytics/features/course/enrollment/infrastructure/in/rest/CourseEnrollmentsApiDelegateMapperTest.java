package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.infrastructure.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseEnrollmentsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.CourseEnrollments;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Group;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.Role;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.User;
import es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model.UserCourse;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class CourseEnrollmentsApiDelegateMapperTest {

  private final CourseEnrollmentsApiDelegateMapper mapper =
      Mappers.getMapper(CourseEnrollmentsApiDelegateMapper.class);

  @BeforeEach
  void setRequestContext() {
    RequestContextHolder.setRequestAttributes(
        new ServletRequestAttributes(new MockHttpServletRequest()));
  }

  @AfterEach
  void clearRequestContext() {
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  void mapsUserMembershipIdsAndUniqueCourseGroupsRolesAndCourses() {
    Group group = new Group(7L, "Test Group", null);
    Role role = new Role(5L, "Student");
    Course course = new Course(2L, "Test Course", "TC", null);
    UserCourse userCourse = new UserCourse(course, 1234L);

    User firstUser = user(13L);
    User secondUser = user(14L);
    CourseEnrollments enrollments =
        new CourseEnrollments(
            2L,
            List.of(
                new Enrollment(firstUser, List.of(role), List.of(group), List.of(userCourse)),
                new Enrollment(
                    secondUser,
                    List.of(new Role(5L, "Student")),
                    List.of(new Group(7L, "Test Group", null)),
                    List.of(userCourse))));

    CourseEnrollmentsResponseDto response =
        mapper.toDto(enrollments, SessionData.builder().build());

    assertEquals(1, response.getGroups().size());
    assertEquals(7L, response.getGroups().get(0).getId());
    assertEquals("Test Group", response.getGroups().get(0).getName());
    assertEquals(1, response.getRoles().size());
    assertEquals(5L, response.getRoles().get(0).getId());
    assertEquals("Student", response.getRoles().get(0).getName());
    assertEquals(1, response.getCourses().size());
    assertEquals(2L, response.getCourses().get(0).getId());
    assertEquals("Test Course", response.getCourses().get(0).getFullName());

    assertEquals(7L, response.getUsers().get(0).getGroups().get(0).getId());
    assertEquals(5L, response.getUsers().get(0).getRoles().get(0).getId());
    assertEquals(2L, response.getUsers().get(0).getCourses().get(0).getId());
    assertEquals(1234L, response.getUsers().get(0).getLastCourseAccess());
  }

  private User user(Long id) {
    return new User(id, "Test User " + id, "Test", "User", "test@example.com", 0L, 0L, null);
  }
}
