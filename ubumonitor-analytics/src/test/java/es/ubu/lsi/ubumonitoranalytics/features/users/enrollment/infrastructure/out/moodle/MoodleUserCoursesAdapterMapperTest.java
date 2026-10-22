package es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.infrastructure.out.moodle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import es.ubu.lsi.moodle.model.core.enrol.getuserscourses.response.GetUsersCoursesResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.users.enrollment.domain.model.Course;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class MoodleUserCoursesAdapterMapperTest {

  private final MoodleUserCoursesAdapterMapper mapper =
      Mappers.getMapper(MoodleUserCoursesAdapterMapper.class);

  @Test
  void mapsMoodleZeroCourseEndDateToNull() {
    GetUsersCoursesResponseApi response = new GetUsersCoursesResponseApi();
    response.setStartdate(1_700_000_000L);
    response.setEnddate(0L);

    Course course = mapper.toDomainCourse(response);

    assertEquals(LocalDateTime.of(2023, 11, 14, 22, 13, 20), course.getStartDate());
    assertNull(course.getEndDate());
  }
}
