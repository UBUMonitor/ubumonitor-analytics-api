package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.CourseLogsApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.GetCourseLogsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.ImportCourseLogsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.in.FetchCourseLogsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.FetchCourseLogsResult;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.GetCourseLogsCommand;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.in.ImportCourseLogsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.ProcessLogsResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseLogsApiDelegateImpl implements CourseLogsApiDelegate {

    private final ImportCourseLogsUseCase importCourseLogsUseCase;
    private final FetchCourseLogsUseCase fetchCourseLogsUseCase;

    private final ImportLogsMapper importLogsMapper;
    private final GetLogsMapper getLogsMapper;

    @Override
    public ResponseEntity<ImportCourseLogsResponseDto> importCourseLogs(Integer courseId, MultipartFile file) {
        ProcessLogsResult processLogsResult = importCourseLogsUseCase.process(courseId, file);
        return ResponseEntity.ok(importLogsMapper.toDto(processLogsResult));
    }


    @Override
    public ResponseEntity<GetCourseLogsResponseDto> getCourseLogs(String courseId, String startDateTime, String endDateTime, List<String> columns, List<Integer> usersIds, List<Integer> modulesIds, List<Integer> componentsIds, List<Integer> eventsIds, List<Integer> originsIds, List<String> ipAddresses, Integer page, Integer size, List<String> sort, Boolean includeTotal) {
        GetCourseLogsCommand getCourseLogsCommand =  getLogsMapper.toDomain(courseId, startDateTime, endDateTime, columns, usersIds, modulesIds, componentsIds, eventsIds, originsIds, ipAddresses, page, size, sort, includeTotal);
        FetchCourseLogsResult fetchCourseLogsResult = fetchCourseLogsUseCase.getCourseLogs(getCourseLogsCommand);
        return ResponseEntity.ok(getLogsMapper.toDto(fetchCourseLogsResult));
    }



}

