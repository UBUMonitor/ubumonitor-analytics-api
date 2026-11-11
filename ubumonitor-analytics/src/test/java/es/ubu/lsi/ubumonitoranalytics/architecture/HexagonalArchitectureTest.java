package es.ubu.lsi.ubumonitoranalytics.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
    packages = "es.ubu.lsi.ubumonitoranalytics",
    importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

  @ArchTest
  static final ArchRule applicationAndDomainDoNotDependOnInfrastructure =
      noClasses()
          .that()
          .resideInAnyPackage("..application..", "..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..infrastructure..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule domainDoesNotDependOnApplication =
      noClasses()
          .that()
          .resideInAnyPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..application..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule applicationAndDomainDoNotDependOnSpringWebOrServlet =
      noClasses()
          .that()
          .resideInAnyPackage("..application..", "..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("org.springframework.web..", "jakarta.servlet..")
          .allowEmptyShould(true);

  @ArchTest
  static final ArchRule outputPortsAreInterfaces =
      classes()
          .that()
          .areTopLevelClasses()
          .and()
          .resideInAPackage("..application.port.out..")
          .should()
          .beInterfaces()
          .allowEmptyShould(true);
}
