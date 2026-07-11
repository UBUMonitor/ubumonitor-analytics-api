package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.CourseLogsApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsInfoRequestDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsInfoResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsMetricsRequestDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsMetricsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseLogsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.in.FetchCourseLogsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.in.ImportCourseLogsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.importlogs.ProcessLogsResult;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info.CourseLogsInfoRequest;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.info.FetchCourseLogsResult;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics.CourseLogsMetricsRequest;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.metrics.CourseLogsMetricsResult;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.in.rest.mapper.GetCourseLogsMapper;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.in.rest.mapper.GetCourseLogsMetricsMapper;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.in.rest.mapper.ImportLogsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CourseLogsApiDelegateImpl implements CourseLogsApiDelegate {

  private final ImportCourseLogsUseCase importCourseLogsUseCase;
  private final FetchCourseLogsUseCase fetchCourseLogsUseCase;

  private final ImportLogsMapper importLogsMapper;
  private final GetCourseLogsMapper getCourseLogsMapper;
  private final GetCourseLogsMetricsMapper getCourseLogsMetricsMapper;

  @Override
  public ResponseEntity<CourseLogsResponseDto> syncCourseLogs(Integer courseId) {
    ProcessLogsResult processLogsResult = importCourseLogsUseCase.sync(courseId);
    return ResponseEntity.ok(importLogsMapper.toDto(processLogsResult));
  }

  @Override
  public ResponseEntity<CourseLogsResponseDto> importCourseLogs(
      Integer courseId, MultipartFile file) {
    ProcessLogsResult processLogsResult = importCourseLogsUseCase.process(courseId, file);
    return ResponseEntity.ok(importLogsMapper.toDto(processLogsResult));
  }

  @Override
  public ResponseEntity<CourseLogsInfoResponseDto> getCourseLogs(
      Integer courseId, CourseLogsInfoRequestDto courseLogsInfoRequestDto) {
    CourseLogsInfoRequest courseLogsInfoRequest =
        getCourseLogsMapper.toDomain(courseId, courseLogsInfoRequestDto);
    FetchCourseLogsResult fetchCourseLogsResult =
        fetchCourseLogsUseCase.getCourseLogs(courseLogsInfoRequest);
    return ResponseEntity.ok(getCourseLogsMapper.toDto(fetchCourseLogsResult));
  }

  @Override
  public ResponseEntity<CourseLogsMetricsResponseDto> getCourseLogsMetrics(
      Integer courseId, CourseLogsMetricsRequestDto courseLogsMetricsRequestDto) {
    CourseLogsMetricsRequest courseLogsMetricsRequest =
        getCourseLogsMetricsMapper.toDomain(courseId, courseLogsMetricsRequestDto);
    CourseLogsMetricsResult result =
        fetchCourseLogsUseCase.getCourseLogsMetrics(courseLogsMetricsRequest);
    return ResponseEntity.ok(getCourseLogsMetricsMapper.toDto(result));
  }
}
