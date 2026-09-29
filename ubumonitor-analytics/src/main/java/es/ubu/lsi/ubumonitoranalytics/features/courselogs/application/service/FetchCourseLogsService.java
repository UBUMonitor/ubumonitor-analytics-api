package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.in.FetchCourseLogsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out.CourseLogsInfoPort;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out.FetchLogPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out.LogsMetricsPort;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info.CourseLogsInfoResult;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.list.CourseLogsInfoRequest;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.list.FetchCourseLogsResult;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics.CourseLogsMetricsRequest;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics.CourseLogsMetricsResult;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics.MetricRow;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FetchCourseLogsService implements FetchCourseLogsUseCase {

  private final FetchLogPersistencePort fetchLogPersistencePort;
  private final LogsMetricsPort logsMetricsPort;
  private final CourseLogsInfoPort courseLogsInfoPort;
  private final TimeSeriesFiller timeSeriesFiller;

  @Override
  public FetchCourseLogsResult getCourseLogs(CourseLogsInfoRequest courseLogsInfoRequest) {
    return fetchLogPersistencePort.getLogs(courseLogsInfoRequest);
  }

  @Override
  public CourseLogsMetricsResult getCourseLogsMetrics(CourseLogsMetricsRequest request) {

    CourseLogsMetricsResult result = logsMetricsPort.getCourseLogsMetrics(request);

    if (request.getInterval() == null) {
      return result;
    }

    List<MetricRow> filledRows = timeSeriesFiller.fill(result.getRows(), request);

    return CourseLogsMetricsResult.builder().rows(filledRows).build();
  }

  @Override
  public CourseLogsInfoResult getCourseLogsInfo(Integer courseId) {
    return courseLogsInfoPort.getCourseLogsInfo(courseId);
  }
}
