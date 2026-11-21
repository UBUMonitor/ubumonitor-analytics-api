package es.ubu.lsi.ubumonitoranalytics.features.system.database.infrastructure.in.rest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.DatabaseSnapshotRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CreateDatabaseSnapshotRequestValidationTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void acceptsUnicodeLetterAndNumberNames() {
    Set<ConstraintViolation<DatabaseSnapshotRequestDto>> violations =
        validator.validate(new DatabaseSnapshotRequestDto().name("Café 東京 123"));

    assertTrue(violations.isEmpty());
  }

  @Test
  void rejectsUnsupportedNamesAndOverlongNames() {
    Set<ConstraintViolation<DatabaseSnapshotRequestDto>> emoji =
        validator.validate(new DatabaseSnapshotRequestDto().name("snapshot 😀"));
    Set<ConstraintViolation<DatabaseSnapshotRequestDto>> overlong =
        validator.validate(new DatabaseSnapshotRequestDto().name("a".repeat(65)));

    assertFalse(emoji.isEmpty());
    assertFalse(overlong.isEmpty());
  }
}
