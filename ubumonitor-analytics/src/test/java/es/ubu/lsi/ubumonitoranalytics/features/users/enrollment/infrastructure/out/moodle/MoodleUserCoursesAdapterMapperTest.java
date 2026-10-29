package es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.infrastructure.out.moodle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import es.ubu.lsi.moodle.model.core.enrol.getuserscourses.response.GetUsersCoursesResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.domain.model.Course;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class MoodleUserCoursesAdapterMapperTest {

  private final MoodleUserCoursesAdapterMapper mapper =
      Mappers.getMapper(MoodleUserCoursesAdapterMapper.class);

  @Test
  void mapsMoodleZeroCourseEndDateToNull() {
    GetUsersCoursesResponseApi response = new GetUsersCoursesResponseApi();
    response.setStartdate(1_700_000_000L);
    response.setTimemodified(0L);
    response.setEnddate(0L);
    response.setLastaccess(0L);

    Course course = mapper.toDomainCourse(response);
    var enrollment = mapper.toDomainEnrolledCourse(response);

    assertEquals(1_700_000_000L, course.getStartDate());
    assertNull(course.getTimeModified());
    assertNull(course.getEndDate());
    assertNull(enrollment.getLastCourseAccess());
  }
}
