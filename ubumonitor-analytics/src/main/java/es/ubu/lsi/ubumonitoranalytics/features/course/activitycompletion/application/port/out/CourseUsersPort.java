package es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.application.port.out;

import java.util.List;

public interface CourseUsersPort {
  List<Long> getCourseUsersIds(Long courseId);
}
