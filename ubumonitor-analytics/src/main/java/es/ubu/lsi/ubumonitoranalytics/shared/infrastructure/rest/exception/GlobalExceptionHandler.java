package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.rest.exception;

import es.ubu.lsi.moodle.exception.MoodleUnauthorizedException;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.ErrorResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.course.courselogs.application.exception.MoodleLogExportException;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.*;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.DatabaseCreationException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.DatabaseMigrationValidationException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.InvalidDatabaseEncryptionKeyException;
import java.util.StringJoiner;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Maps application and framework exceptions to the API error response contract.
 *
 * <p>Contains only specific handlers. Wrapped exceptions (for example {@code CompletionException})
 * are resolved by Spring through their cause chain, so each handler must declare the concrete
 * exception type as its parameter.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final ErrorResponseFactory factory;

  /** Handles application bad-request errors. */
  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorResponseDto> badRequest(BadRequestException ex) {
    return response(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), ex);
  }

  /** Handles requests that omit a required query parameter. */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponseDto> missingRequestParameter(
      MissingServletRequestParameterException ex) {
    return response(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), ex);
  }

  /** Handles malformed request bodies. */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponseDto> unreadableRequest(HttpMessageNotReadableException ex) {
    return response(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), ex);
  }

  /** Handles authentication failures from the application. */
  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorResponseDto> unauthorized(UnauthorizedException ex) {
    return response(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage(), ex);
  }

  /** Handles authentication failures returned by Moodle. */
  @ExceptionHandler(MoodleUnauthorizedException.class)
  public ResponseEntity<ErrorResponseDto> moodleUnauthorized(MoodleUnauthorizedException ex) {
    return response(HttpStatus.UNAUTHORIZED, "MOODLE_UNAUTHORIZED", ex.getMessage(), ex);
  }

  /** Handles invalid responses from Moodle's course log export. */
  @ExceptionHandler(MoodleLogExportException.class)
  public ResponseEntity<ErrorResponseDto> moodleLogExport(MoodleLogExportException ex) {
    return response(HttpStatus.INTERNAL_SERVER_ERROR, "UNKNOWN_ERROR", ex.getMessage(), ex);
  }

  /** Handles application not-found errors. */
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponseDto> notFound(NotFoundException ex) {
    return response(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), ex);
  }

  /** Handles application conflict errors. */
  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorResponseDto> conflict(ConflictException ex) {
    return response(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), ex);
  }

  /** Handles tenant database creation failures. */
  @ExceptionHandler(DatabaseCreationException.class)
  public ResponseEntity<ErrorResponseDto> databaseCreation(DatabaseCreationException ex) {
    return response(
        HttpStatus.INTERNAL_SERVER_ERROR, "DATABASE_CREATION_ERROR", ex.getMessage(), ex);
  }

  @ExceptionHandler(DatabaseMigrationValidationException.class)
  public ResponseEntity<ErrorResponseDto> databaseMigrationValidation(
      DatabaseMigrationValidationException ex) {
    return response(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "DATABASE_MIGRATION_VALIDATION_ERROR",
        ex.getMessage(),
        ex);
  }

  /** Handles tenant database passwords that cannot open an encrypted database. */
  @ExceptionHandler(InvalidDatabaseEncryptionKeyException.class)
  public ResponseEntity<ErrorResponseDto> invalidDatabaseEncryptionKey(
      InvalidDatabaseEncryptionKeyException ex) {
    return response(
        HttpStatus.UNAUTHORIZED,
        "INVALID_DATABASE_ENCRYPTION_KEY",
        "The database password is incorrect.",
        ex);
  }

  /** Handles bean-validation failures and collects field messages. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDto> validationError(MethodArgumentNotValidException ex) {
    StringJoiner joiner = new StringJoiner(", ");
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
      joiner.add(fieldError.getField() + ": " + fieldError.getDefaultMessage());
    }
    return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", joiner.toString(), ex);
  }

  /** Handles requests for resources that are not mapped by Spring MVC. */
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponseDto> resourceNotFound(NoResourceFoundException ex) {
    return response(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), ex);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> internalServerError(Exception ex) {
    return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", ex.getMessage(), ex);
  }

  private ResponseEntity<ErrorResponseDto> response(
      HttpStatus status, String code, String message, Throwable ex) {
    return ResponseEntity.status(status).body(factory.build(code, message, ex));
  }
}
