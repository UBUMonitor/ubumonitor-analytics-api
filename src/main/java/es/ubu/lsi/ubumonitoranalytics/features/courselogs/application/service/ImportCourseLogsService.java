package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.in.ImportCourseLogsUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out.LogPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.LogLine;
import es.ubu.lsi.ubumonitoranalytics.features.courselogs.domain.model.ProcessLogsResult;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportCourseLogsService implements ImportCourseLogsUseCase {

    private static final int BATCH_SIZE = 10_000;

    private final LogParserService logParserService;
    private final LogPersistencePort logPersistencePort;

    @Override
    @SneakyThrows
    public ProcessLogsResult process(Integer courseId, MultipartFile file) {

        Map<String, Byte> logComponents = logPersistencePort.getLogComponents();
        Map<String, Short> logEvents = logPersistencePort.getLogEvents();
        Map<String, Byte> logOrigins = logPersistencePort.getLogOrigins();

        LocalDateTime lastLogDateTime = logPersistencePort.getLastDateTime(courseId);

        List<LogLine> batch = new ArrayList<>(BATCH_SIZE);

        int saved = 0;
        int ignored = 0;
        int failed = 0;
        try (
            BufferedReader reader = buildReader(file);
            CSVParser parser = buildCsvParser(reader)
        ) {

            for (CSVRecord csvRecord : parser) {
                LogLine logLine;
                try {
                     logLine = logParserService.processRow(
                        courseId,
                        csvRecord,
                        logComponents,
                        logEvents,
                         logOrigins
                    );
                } catch (Exception e) {
                    log.warn("Error while processing log line: {}", csvRecord, e);
                    failed++;
                    continue;
                }


                if (isValidLog(logLine, lastLogDateTime)) {
                    batch.add(logLine);

                    if (batch.size() >= BATCH_SIZE) {
                        saved += persistBatch(batch);
                    }
                } else {
                    ignored++;
                }


            }

            saved += persistBatch(batch);
        }

        return new ProcessLogsResult(saved, ignored, failed);
    }
    private int persistBatch(List<LogLine> batch) {

        if (batch.isEmpty()) {
            return 0;
        }

        int size = batch.size();

        logPersistencePort.saveBatch(batch);

        batch.clear();

        return size;
    }

    private boolean isValidLog(LogLine logLine, LocalDateTime lastLogDateTime) {
        return logLine != null
            && (lastLogDateTime == null || lastLogDateTime.isBefore(logLine.getTime()));
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
