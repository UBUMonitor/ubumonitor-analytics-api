package es.ubu.lsi.ubumonitoranalytics.shared.application.exception;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.ErrorDetailDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.ErrorResponseDto;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/** Builds API error bodies and records diagnostic details for failures. */
@Component
@Slf4j
public class ErrorResponseFactory {

  public ErrorResponseDto build(String code, String message, Throwable ex) {
    log.error("Error occurred", ex);
    List<ErrorDetailDto> errors = new ArrayList<>();

    if (ex != null) {
      errors.add(new ErrorDetailDto().field("stackTrace").message(getStackTrace(ex)));
    }

    return new ErrorResponseDto()
        .code(code)
        .message(message)
        .timestamp(OffsetDateTime.now())
        .traceId(getTraceId())
        .errors(errors);
  }

  /** Builds a compact representation of the cause chain, with each exception on a separate line. */
  private String getStackTrace(Throwable ex) {
    if (ex == null) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    Throwable current = ex;
    int depth = 0;

    while (current != null) {
      String indent = "  ".repeat(depth);

      if (depth > 0) {
        sb.append(System.lineSeparator()).append(indent).append("Caused by: ");
      }

      sb.append(current.getClass().getName());
      String msg = current.getMessage();
      if (msg != null && !msg.isEmpty()) {
        sb.append(": ").append(msg);
      }

      StackTraceElement[] stackTrace = current.getStackTrace();
      int limit = Math.min(stackTrace.length, 10); // Limit frames to keep logs concise.
      for (int i = 0; i < limit; i++) {
        sb.append(System.lineSeparator()).append(indent).append("    at ").append(stackTrace[i]);
      }
      if (stackTrace.length > limit) {
        sb.append(System.lineSeparator())
            .append(indent)
            .append("    ... ")
            .append(stackTrace.length - limit)
            .append(" more");
      }

      current = current.getCause();
      depth++;
    }

    return sb.toString();
  }

  private String getTraceId() {
    String traceId = MDC.get("traceId");
    return traceId != null ? traceId : UUID.randomUUID().toString();
  }
}
