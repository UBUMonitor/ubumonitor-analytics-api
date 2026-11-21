package es.ubu.lsi.ubumonitoranalytics.features.system.database.infrastructure.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.DatabaseSnapshotRequestDto;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.ErrorResponseFactory;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.rest.exception.GlobalExceptionHandler;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class CreateDatabaseSnapshotRequestValidationTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void acceptsOnlyNormalAsciiNames() {
    List<String> validNames = List.of("abc", "antes de sincronizar", "v1.2_final-3", "a", "A1");

    for (String name : validNames) {
      Set<ConstraintViolation<DatabaseSnapshotRequestDto>> violations =
          validator.validate(new DatabaseSnapshotRequestDto().name(name));
      assertTrue(violations.isEmpty(), name);
    }
  }

  @Test
  void rejectsInvalidNamesOnTheNameField() {
    List<String> invalidNames =
        List.of(
            "ñandú",
            "café",
            "snapshot 😀",
            "a/b",
            "a\\b",
            "a~b",
            "a%b",
            "a+b",
            "a:b",
            "a*b",
            "a?b",
            "a\"b",
            "a<b",
            "a>b",
            "a|b",
            ".",
            "..",
            " abc ",
            "-x",
            "x-",
            "x.",
            "a\nb",
            "a".repeat(65));

    for (String name : invalidNames) {
      Set<ConstraintViolation<DatabaseSnapshotRequestDto>> violations =
          validator.validate(new DatabaseSnapshotRequestDto().name(name));
      assertTrue(
          violations.stream()
              .anyMatch(violation -> violation.getPropertyPath().toString().equals("name")),
          name);
    }
  }

  @Test
  void globalHandlerMapsBeanValidationErrorsToValidationError() throws Exception {
    DatabaseSnapshotRequestDto request = new DatabaseSnapshotRequestDto().name("café");
    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(request, "databaseSnapshotRequest");
    bindingResult.addError(new FieldError("databaseSnapshotRequest", "name", "must match pattern"));
    Method requestMethod =
        getClass().getDeclaredMethod("request", DatabaseSnapshotRequestDto.class);
    MethodArgumentNotValidException exception =
        new MethodArgumentNotValidException(new MethodParameter(requestMethod, 0), bindingResult);
    GlobalExceptionHandler handler = new GlobalExceptionHandler(new ErrorResponseFactory());

    var response = handler.validationError(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("VALIDATION_ERROR", response.getBody().getCode());
  }

  private void request(DatabaseSnapshotRequestDto request) {}
}
