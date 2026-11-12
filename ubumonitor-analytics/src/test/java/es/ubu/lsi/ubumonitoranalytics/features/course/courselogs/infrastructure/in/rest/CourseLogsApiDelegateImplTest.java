package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.in.rest;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.in.FetchCourseLogsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.port.in.ImportCourseLogsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.in.rest.mapper.ColumnInfoMapper;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.in.rest.mapper.GetCourseLogsMapper;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.in.rest.mapper.GetCourseLogsStatisticsMapper;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.infrastructure.in.rest.mapper.ImportLogsMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

class CourseLogsApiDelegateImplTest {

  @Test
  void wrapsIOExceptionWhenOpeningUploadedFileAsInternalServerError() throws IOException {
    ImportCourseLogsUseCase importCourseLogsUseCase = mock(ImportCourseLogsUseCase.class);
    MultipartFile file = mock(MultipartFile.class);
    IOException cause = new IOException("stream unavailable");
    when(file.getInputStream()).thenThrow(cause);

    CourseLogsApiDelegateImpl delegate =
        new CourseLogsApiDelegateImpl(
            importCourseLogsUseCase,
            mock(FetchCourseLogsUseCase.class),
            mock(ImportLogsMapper.class),
            mock(GetCourseLogsMapper.class),
            mock(GetCourseLogsStatisticsMapper.class),
            mock(ColumnInfoMapper.class));

    UncheckedIOException exception =
        assertThrows(UncheckedIOException.class, () -> delegate.importCourseLogs(12L, file));

    assertSame(cause, exception.getCause());
    verifyNoInteractions(importCourseLogsUseCase);
  }
}
