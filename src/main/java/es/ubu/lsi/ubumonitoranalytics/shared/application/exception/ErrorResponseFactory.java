package es.ubu.lsi.ubumonitoranalytics.shared.application.exception;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.ErrorDetailDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.ErrorResponseDto;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class ErrorResponseFactory {

    public ErrorResponseDto build(String code, String message, Throwable ex) {

        List<ErrorDetailDto> errors = new ArrayList<>();

        if (ex != null) {
            errors.add(
                new ErrorDetailDto()
                    .field("stackTrace")
                    .message(getStackTrace(ex))
            );
        }

        return new ErrorResponseDto()
            .code(code)
            .message(message)
            .timestamp(OffsetDateTime.now())
            .traceId(getTraceId())
            .errors(errors);
    }

    /**
     * Devuelve una representación compacta formada por los mensajes de la cadena de causas.
     * Cada excepción se representa como "<ClaseExcepción>: <mensaje>" en una nueva línea.
     */
    private String getStackTrace(Throwable ex) {
        if (ex == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        Throwable current = ex;
        boolean first = true;
        while (current != null) {
            if (!first) {
                sb.append(System.lineSeparator()).append("caused by ");
            }
            first = false;
            String className = current.getClass().getSimpleName();
            String msg = current.getMessage();
            sb.append(className);
            if (msg != null && !msg.isEmpty()) {
                sb.append(": ").append(msg);
            }
            current = current.getCause();
        }

        return sb.toString();
    }

    private String getTraceId() {
        String traceId = MDC.get("traceId");
        return traceId != null ? traceId : UUID.randomUUID().toString();
    }
}
