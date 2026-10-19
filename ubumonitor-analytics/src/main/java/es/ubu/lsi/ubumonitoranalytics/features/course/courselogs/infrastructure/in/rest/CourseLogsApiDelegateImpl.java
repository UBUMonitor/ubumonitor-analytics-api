package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.CourseLogsApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.*;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.in.FetchCourseLogsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.in.ImportCourseLogsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.ProcessLogsResult;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.info.CourseLogsInfoResult;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.list.CourseLogsInfoRequest;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.list.FetchCourseLogsResult;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.CourseLogsStatisticsRequest;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.statistics.CourseLogsStatisticsResult;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.in.rest.mapper.ColumnInfoMapper;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.in.rest.mapper.GetCourseLogsMapper;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.in.rest.mapper.GetCourseLogsStatisticsMapper;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.in.rest.mapper.ImportLogsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** Implements the generated REST operations for course logs. */
@Service
@RequiredArgsConstructor
public class CourseLogsApiDelegateImpl implements CourseLogsApiDelegate {

  private final ImportCourseLogsUseCase importCourseLogsUseCase;
  private final FetchCourseLogsUseCase fetchCourseLogsUseCase;

  private final ImportLogsMapper importLogsMapper;
  private final GetCourseLogsMapper getCourseLogsMapper;
  private final GetCourseLogsStatisticsMapper getCourseLogsStatisticsMapper;
  private final ColumnInfoMapper columnInfoMapper;

  @Override
  public ResponseEntity<CourseLogsResponseDto> syncCourseLogs(Long courseId) {

    ProcessLogsResult processLogsResult = importCourseLogsUseCase.sync(courseId);

    return ResponseEntity.ok(importLogsMapper.toDto(processLogsResult));
  }

  @Override
  public ResponseEntity<CourseLogsResponseDto> importCourseLogs(Long courseId, MultipartFile file) {

    ProcessLogsResult processLogsResult = importCourseLogsUseCase.process(courseId, file);

    return ResponseEntity.ok(importLogsMapper.toDto(processLogsResult));
  }

  @Override
  public ResponseEntity<CourseLogsResponseDto> getCourseLogsInfo(Long courseId) {
    CourseLogsInfoResult courseLogsInfoResult = fetchCourseLogsUseCase.getCourseLogsInfo(courseId);
    return ResponseEntity.ok(columnInfoMapper.toDto(courseLogsInfoResult));
  }

  @Override
  public ResponseEntity<CourseLogsListResponseDto> getCourseLogsList(
      Long courseId, CourseLogsListRequestDto courseLogsListRequestDto) {
    CourseLogsInfoRequest courseLogsInfoRequest =
        getCourseLogsMapper.toDomain(courseId, courseLogsListRequestDto);

    FetchCourseLogsResult fetchCourseLogsResult =
        fetchCourseLogsUseCase.getCourseLogs(courseLogsInfoRequest);

    return ResponseEntity.ok(getCourseLogsMapper.toDto(fetchCourseLogsResult));
  }

  @Override
  public ResponseEntity<CourseLogsStatisticsResponseDto> getCourseLogsStatistics(
      Long courseId, CourseLogsStatisticsRequestDto courseLogsStatisticsRequestDto) {

    CourseLogsStatisticsRequest courseLogsStatisticsRequest =
        getCourseLogsStatisticsMapper.toDomain(courseId, courseLogsStatisticsRequestDto);

    CourseLogsStatisticsResult result =
        fetchCourseLogsUseCase.getCourseLogsStatistics(courseLogsStatisticsRequest);

    return ResponseEntity.ok(getCourseLogsStatisticsMapper.toDto(result));
  }
}
