package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info.CourseLogsInfoResult;

public interface CourseLogsInfoPort {
  CourseLogsInfoResult getCourseLogsInfo(Integer courseId);
}
