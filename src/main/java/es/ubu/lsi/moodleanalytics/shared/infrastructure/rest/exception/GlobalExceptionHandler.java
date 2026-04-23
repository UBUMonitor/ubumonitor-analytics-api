package es.ubu.lsi.moodleanalytics.shared.infrastructure.rest.exception;

import es.ubu.lsi.moodleanalytics.api.generated.model.ErrorDetailDto;
import es.ubu.lsi.moodleanalytics.api.generated.model.ErrorResponseDto;
import es.ubu.lsi.moodleanalytics.shared.application.exception.NotFoundException;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleSessionNotFound(NotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(toErrorResponse(
                        "UNAUTHORIZED",
                        ex.getMessage(),
                        null
                ));
    }

    private ErrorResponseDto toErrorResponse(String code, String message, List<ErrorDetailDto> errors) {

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                code,
                message,
                OffsetDateTime.now()

        );
        errorResponseDto.traceId( getTraceId())
                .errors(errors);

        return errorResponseDto;
    }


    private String getTraceId() {
        // si usas MDC (recomendado)
        String traceId = MDC.get("traceId");

        return traceId != null ? traceId : java.util.UUID.randomUUID().toString();
    }
}
