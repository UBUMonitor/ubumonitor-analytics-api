package es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.input.BOMInputStream;

/** Opens Moodle log CSV exports, whether they come from an upload or from a Moodle download. */
public final class MoodleCsvReader {

  private static final int BUFFER_SIZE = 64 * 1024;

  /** The first row is the header and is not returned as a record. */
  private static final CSVFormat FORMAT =
      CSVFormat.DEFAULT
          .builder()
          .setHeader()
          .setSkipHeaderRecord(true)
          .setIgnoreSurroundingSpaces(true)
          .setTrim(true)
          .get();

  private MoodleCsvReader() {}

  /**
   * Parses the stream as UTF-8 CSV, skipping a BOM if present. Closing the returned parser also
   * closes the stream.
   *
   * @param inputStream raw CSV bytes
   * @return parser positioned on the first data record
   * @throws IOException if the header cannot be read
   */
  public static CSVParser open(InputStream inputStream) throws IOException {
    InputStream withoutBom = BOMInputStream.builder().setInputStream(inputStream).get();
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(withoutBom, StandardCharsets.UTF_8), BUFFER_SIZE);
    try {
      return FORMAT.parse(reader);
    } catch (IOException | RuntimeException exception) {
      try {
        reader.close();
      } catch (IOException closeException) {
        exception.addSuppressed(closeException);
      }
      throw exception;
    }
  }
}
