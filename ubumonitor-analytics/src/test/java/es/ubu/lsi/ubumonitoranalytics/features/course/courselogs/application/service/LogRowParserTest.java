package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.csv.MoodleCsvReader;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.LogContext;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.domain.model.importlogs.MoodleLogCatalog;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
class LogRowParserTest {

  private static final String CSV_HEADER =
      "Time,Component,Event name,Origin,IP address,Description\n";

  @Test
  void invalidDateRecordIsCountedAsFailedAndFollowingRowsAreParsed() throws Exception {
    String csv =
        CSV_HEADER
            + "not-a-date,Activity,Viewed,web,127.0.0.1,description\n"
            + "\"25/10/26, 02:30:00\",Activity,Viewed,web,127.0.0.1,description\n";
    LogRowParser parser = newParser(null);

    for (CSVRecord record : records(csv)) {
      parser.parse(record);
    }

    assertEquals(1, parser.getFailed());
    assertEquals(0, parser.getIgnored());
    assertEquals(1, parser.getLines().size());
  }

  @Test
  void missingNamedColumnIsCountedAsFailed() throws Exception {
    LogRowParser parser = newParser(null);

    for (CSVRecord record : records("Time\n\"25/10/26, 02:30:00\"\n")) {
      parser.parse(record);
    }

    assertEquals(1, parser.getFailed());
    assertEquals(0, parser.getIgnored());
    assertEquals(0, parser.getLines().size());
  }

  @Test
  void nullRowIsCountedAsFailedWithoutEscaping() {
    LogRowParser parser = newParser(null);

    parser.parse(null);

    assertEquals(1, parser.getFailed());
    assertEquals(0, parser.getIgnored());
    assertEquals(0, parser.getLines().size());
  }

  @Test
  void failureLogContainsRowAndErrorTypeButNotCsvValues(CapturedOutput output) throws Exception {
    LogRowParser parser = newParser(null);
    CSVRecord record =
        records(CSV_HEADER + "not-a-date,Activity,Viewed,web,127.0.0.1,secret-sentinel\n")
            .getFirst();

    parser.parse(record);

    assertEquals(1, parser.getFailed());
    assertTrue(output.getAll().contains("CSV row 1"));
    assertTrue(output.getAll().contains("DateTimeParseException"));
    assertFalse(output.getAll().contains("secret-sentinel"));
  }

  @Test
  void localDateTimeUsesMoodleZoneAndJavaDstResolution() throws Exception {
    LogRowParser parser = newParser(null);

    for (CSVRecord record :
        records(
            CSV_HEADER
                + "\"25/10/26, 02:30:00\",Activity,Viewed,web,127.0.0.1,description\n"
                + "\"29/03/26, 02:30:00\",Activity,Viewed,web,127.0.0.1,description\n")) {
      parser.parse(record);
    }

    assertEquals(
        List.of(Instant.parse("2026-10-25T00:30:00Z"), Instant.parse("2026-03-29T01:30:00Z")),
        parser.getLines().stream().map(line -> line.getTime()).toList());
  }

  @Test
  void rowsAtOrBeforeLastStoredTimeAreIgnoredButDuplicateNewRowsAreRetained() throws Exception {
    Instant lastLogTime = Instant.parse("2026-10-25T00:30:00Z");
    String csv =
        CSV_HEADER
            + "\"25/10/26, 02:30:00\",Activity,Viewed,web,127.0.0.1,description\n"
            + "\"25/10/26, 02:31:00\",Activity,Viewed,web,127.0.0.1,description\n"
            + "\"25/10/26, 02:31:00\",Activity,Viewed,web,127.0.0.1,description\n";
    LogRowParser parser = newParser(lastLogTime);

    for (CSVRecord record : records(csv)) {
      parser.parse(record);
    }

    assertEquals(1, parser.getIgnored());
    assertEquals(0, parser.getFailed());
    assertEquals(2, parser.getLines().size());
    assertEquals(parser.getLines().getFirst().getTime(), parser.getLines().getLast().getTime());
  }

  private static LogRowParser newParser(Instant lastLogTime) {
    MoodleLogCatalog.Key key = new MoodleLogCatalog.Key("Activity", "Viewed");
    MoodleLogCatalog.Entry entry = new MoodleLogCatalog.Entry((short) 1, (short) 2, List.of());
    LogContext context =
        LogContext.builder()
            .courseId(42L)
            .lastLogTime(lastLogTime)
            .moodleTimeZone(ZoneId.of("Europe/Madrid"))
            .logOrigins(Map.of("web", (byte) 3))
            .moodleLogCatalog(new MoodleLogCatalog(Map.of(key, entry)))
            .build();
    return new LogRowParser(new LogParserService(), context);
  }

  private static List<CSVRecord> records(String csv) throws Exception {
    try (CSVParser parser =
        MoodleCsvReader.open(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)))) {
      List<CSVRecord> records = new ArrayList<>();
      for (CSVRecord record : parser) {
        records.add(record);
      }
      return records;
    }
  }
}
