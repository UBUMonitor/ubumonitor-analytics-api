package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.security;

import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.ErrorResponseFactory;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/** Writes authentication failures using the API's standard error response. */
@Component
@RequiredArgsConstructor
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private static final String UNAUTHORIZED_MESSAGE =
      "Authentication is required to access this resource.";

  private final ErrorResponseFactory errorResponseFactory;
  private final ObjectMapper objectMapper;

  @Override
  public void commence(
      jakarta.servlet.http.HttpServletRequest request,
      jakarta.servlet.http.HttpServletResponse response,
      AuthenticationException authenticationException)
      throws IOException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(
        response.getOutputStream(),
        errorResponseFactory.build("UNAUTHORIZED", UNAUTHORIZED_MESSAGE, authenticationException));
  }
}
