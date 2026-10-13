package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.rest.exception;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.ErrorResponseDto;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.ErrorResponseFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Last-resort handler: only runs when no specific handler matches the exception or its causes. */
@Slf4j
@Order()
@RestControllerAdvice
@RequiredArgsConstructor
public class FallbackExceptionHandler {

  private final ErrorResponseFactory factory;

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> unknown(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(factory.build("UNKNOWN_ERROR", ex.getMessage(), ex));
  }
}
