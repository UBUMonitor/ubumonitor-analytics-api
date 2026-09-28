package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.rest.exception;

import es.ubu.lsi.moodle.exception.MoodleUnauthorizedException;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.ErrorResponseDto;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final ErrorResponseFactory factory;

  @ExceptionHandler({BadRequestException.class, HttpMessageNotReadableException.class})
  public ResponseEntity<ErrorResponseDto> badRequest(Exception ex) {
    return ResponseEntity.badRequest().body(factory.build("BAD_REQUEST", ex.getMessage(), ex));
  }

  @ExceptionHandler({UnauthorizedException.class, MoodleUnauthorizedException.class})
  public ResponseEntity<ErrorResponseDto> unauthorizedException(Exception ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(factory.build("UNAUTHORIZED", ex.getMessage(), ex));
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponseDto> notFound(Exception ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(factory.build("NOT_FOUND", ex.getMessage(), ex));
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorResponseDto> conflict(Exception ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(factory.build("CONFLICT", ex.getMessage(), ex));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> unknown(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(factory.build("UNKNOWN_ERROR", ex.getMessage(), ex));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDto> validationError(MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
            .collect(Collectors.joining(", "));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(factory.build("VALIDATION_ERROR", message, ex));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponseDto> resourceNotFound(NoResourceFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(factory.build("RESOURCE_NOT_FOUND", ex.getMessage(), ex));
  }
}
