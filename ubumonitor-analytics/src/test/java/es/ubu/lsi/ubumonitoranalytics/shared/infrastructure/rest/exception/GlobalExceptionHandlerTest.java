package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.rest.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.ubu.lsi.moodle.exception.MoodleUnauthorizedException;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.exception.MoodleLogExportException;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.ErrorResponseFactory;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.UnauthorizedException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.InvalidDatabaseEncryptionKeyException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler exceptionHandler =
      new GlobalExceptionHandler(new ErrorResponseFactory());

  @Test
  void mapsApplicationUnauthorizedExceptionToUnauthorizedResponse() {
    var response = exceptionHandler.unauthorized(new UnauthorizedException("Invalid credentials"));

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("UNAUTHORIZED", response.getBody().getCode());
  }

  @Test
  void mapsMoodleUnauthorizedExceptionToUnauthorizedResponse() {
    var response =
        exceptionHandler.moodleUnauthorized(new MoodleUnauthorizedException("Invalid login"));

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("MOODLE_UNAUTHORIZED", response.getBody().getCode());
  }

  @Test
  void mapsMoodleLogExportExceptionToInternalServerErrorResponse() {
    var response =
        exceptionHandler.moodleLogExport(new MoodleLogExportException("Unexpected Content-Type"));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals("UNKNOWN_ERROR", response.getBody().getCode());
    assertEquals("Unexpected Content-Type", response.getBody().getMessage());
  }

  @Test
  void mapsInvalidDatabaseEncryptionKeyToSpecificResponse() {
    var response =
        exceptionHandler.invalidDatabaseEncryptionKey(
            new InvalidDatabaseEncryptionKeyException("Invalid database encryption key", null));

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("INVALID_DATABASE_ENCRYPTION_KEY", response.getBody().getCode());
    assertEquals("The database password is incorrect.", response.getBody().getMessage());
  }
}
