package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.importlogs.ProcessLogsResult;
import org.springframework.web.multipart.MultipartFile;

/** Imports log records from a file or synchronizes them from Moodle. */
public interface ImportCourseLogsUseCase {

  /**
   * @param course course identifier
   * @param file uploaded CSV file
   * @return import statistics and updated log information
   */
  ProcessLogsResult process(Integer course, MultipartFile file);

  /**
   * @param course course identifier
   * @return synchronization statistics and updated log information
   */
  ProcessLogsResult sync(Integer course);
}
