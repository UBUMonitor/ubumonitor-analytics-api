package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.rest.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.ubu.lsi.moodle.exception.MoodleUnauthorizedException;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.ErrorResponseFactory;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.UnauthorizedException;
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
    assertEquals("UNAUTHORIZED", response.getBody().getCode());
  }
}
