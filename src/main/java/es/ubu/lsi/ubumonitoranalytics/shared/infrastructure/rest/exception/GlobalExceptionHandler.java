package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.rest.exception;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.ErrorResponseDto;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.BadRequestException;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.ConflictException;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.ErrorResponseFactory;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.NotFoundException;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorResponseFactory factory;

    @ExceptionHandler({BadRequestException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponseDto> badRequest(Exception ex) {
        return ResponseEntity
            .badRequest()
            .body(factory.build("BAD_REQUEST", ex.getMessage(), ex));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponseDto> unauthorizedException(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(factory.build("UNAUTHORIZED", ex.getMessage(), ex));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponseDto> notFound(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(factory.build("NOT_FOUND", ex.getMessage(), ex));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDto> conflict(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(factory.build("CONFLICT", ex.getMessage(), ex));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> unknown(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(factory.build("UNKNOWN_ERROR", ex.getMessage(), ex));
    }
}
