package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.in.ImportCourseLogsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out.LogPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.importlogs.ProcessLogLine;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.importlogs.ProcessLogsResult;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportCourseLogsService implements ImportCourseLogsUseCase {


    private final LogParserService logParserService;
    private final LogPersistencePort logPersistencePort;


    @Override
    @SneakyThrows
    public ProcessLogsResult process(Integer courseId, MultipartFile file) {

        if (!logPersistencePort.existCourse(courseId)) {
            throw  new EntityNotFoundException("Course with id " + courseId + " not found");
        }

        Map<String, Byte> logComponents = logPersistencePort.getLogComponents();
        Map<String, Short> logEvents = logPersistencePort.getLogEvents();
        Map<String, Byte> logOrigins = logPersistencePort.getLogOrigins();

        LocalDateTime lastLogDateTime = logPersistencePort.getLastDateTime(courseId);

        AtomicInteger saved = new AtomicInteger(0);
        AtomicInteger ignored = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

        try (
            BufferedReader reader = buildReader(file);
            CSVParser parser = buildCsvParser(reader)
        ) {

            log.info("Starting process CSV logs, last log date: {}", lastLogDateTime);

            List<ProcessLogLine> validLines =
                StreamSupport.stream(parser.spliterator(), true)
                    .map(csvRecord -> {
                        try {
                            ProcessLogLine line = logParserService.processRow(
                                courseId,
                                csvRecord,
                                logComponents,
                                logEvents,
                                logOrigins
                            );

                            if (isValidLog(line, lastLogDateTime)) {
                                return line;
                            } else {
                                ignored.incrementAndGet();
                                return null;
                            }

                        } catch (Exception e) {
                            log.warn("Error while processing log line: {}", csvRecord, e);
                            failed.incrementAndGet();
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();

            log.info("Finished processing CSV logs: {}", validLines.size());

            boolean persisted = persistBatch(validLines);
            if (persisted) {
                saved.addAndGet(validLines.size());

            } else {
                failed.addAndGet(validLines.size());
            }
        }

        return new ProcessLogsResult(
            saved.get(),
            ignored.get(),
            failed.get()
        );
    }


    private boolean persistBatch(List<ProcessLogLine> logLines) {

        if (logLines.isEmpty()) {
            return true;
        }

        try {

            logPersistencePort.saveBatch(logLines);
            return true;

        } catch (Exception e) {
            log.error("Error persisting batch", e);
            return false;
        }
    }

    private boolean isValidLog(ProcessLogLine processLogLine, LocalDateTime lastLogDateTime) {
        return processLogLine != null
            && (lastLogDateTime == null || lastLogDateTime.isBefore(processLogLine.getTime()));
    }

    private BufferedReader buildReader(MultipartFile file) throws IOException {
        return new BufferedReader(
            new InputStreamReader(
                BOMInputStream.builder()
                    .setInputStream(file.getInputStream())
                    .get(),
                StandardCharsets.UTF_8
            ),
            64 * 1024
        );
    }

    private CSVParser buildCsvParser(BufferedReader reader) throws IOException {

        CSVFormat format = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .get();

        return format.parse(reader);
    }
}
