package es.ubu.lsi.ubumonitoranalytics.features.course.grades.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.CourseGrade;

public interface GradesApiPort {
  CourseGrade fetchCourseGradeItems(Integer courseId, Integer actualUserId);
}
