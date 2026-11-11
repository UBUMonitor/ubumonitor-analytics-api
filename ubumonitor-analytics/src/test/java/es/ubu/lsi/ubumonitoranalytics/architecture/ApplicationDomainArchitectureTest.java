package es.ubu.lsi.ubumonitoranalytics.architecture;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class ApplicationDomainArchitectureTest {

  private static final Set<String> EXISTING_INFRASTRUCTURE_IMPORTS =
      Set.of(
          "es/ubu/lsi/ubumonitoranalytics/features/auth/application/service/AuthService.java:"
              + "es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.concurrent.AsyncOperations",
          "es/ubu/lsi/ubumonitoranalytics/features/course/courselogs/application/service/ImportCourseLogsService.java:"
              + "es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.concurrent.AsyncOperations",
          "es/ubu/lsi/ubumonitoranalytics/features/course/courselogs/application/service/LogImportRun.java:"
              + "es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.concurrent.AsyncOperations");

  @Test
  void applicationAndDomainDoNotIntroduceInfrastructureDependencies() throws IOException {
    Path sourceRoot = Path.of("src/main/java");
    List<String> violations = new ArrayList<>();

    try (Stream<Path> sourceFiles = Files.walk(sourceRoot)) {
      for (Path sourceFile :
          sourceFiles.filter(path -> path.toString().endsWith(".java")).toList()) {
        String relativePath = sourceRoot.relativize(sourceFile).toString().replace('\\', '/');
        if (!relativePath.contains("/application/") && !relativePath.contains("/domain/")) {
          continue;
        }
        for (String line : Files.readAllLines(sourceFile)) {
          String importName = importedInfrastructureType(line);
          if (importName != null) {
            violations.add(relativePath + ":" + importName);
          }
        }
      }
    }

    assertEquals(
        EXISTING_INFRASTRUCTURE_IMPORTS,
        violations.stream().collect(Collectors.toSet()),
        "Unexpected application/domain dependency on infrastructure");
  }

  private static String importedInfrastructureType(String line) {
    String importPrefix = "import ";
    int importStart = line.indexOf(importPrefix);
    if (importStart < 0 || !line.contains(".infrastructure.")) {
      return null;
    }

    String importedType = line.substring(importStart + importPrefix.length()).trim();
    if (importedType.endsWith(";")) {
      importedType = importedType.substring(0, importedType.length() - 1);
    }
    return importedType;
  }
}
