package es.ubu.lsi.ubumonitoranalytics.features.grades.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.grades.domain.model.CourseGrade;

public interface GradesPort {
  CourseGrade fetchCourseGradeItems(Integer courseId, Integer actualUserId);
}
