package es.ubu.lsi.ubumonitoranalytics.features.grades.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.grades.domain.model.CourseGrade;

public interface GradesUseCase {

  CourseGrade sync(Integer courseId);

  CourseGrade get(Integer courseId);
}
