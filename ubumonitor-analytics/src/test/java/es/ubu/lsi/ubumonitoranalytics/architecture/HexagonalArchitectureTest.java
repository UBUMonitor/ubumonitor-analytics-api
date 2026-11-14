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
  // Application and domain code must depend on ports, not infrastructure adapters.
  static final ArchRule applicationAndDomainDoNotDependOnInfrastructure =
      noClasses()
          .that()
          .resideInAnyPackage("..application..", "..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..infrastructure..");

  @ArchTest
  // Domain types must not depend on application contracts or services.
  static final ArchRule domainDoesNotDependOnApplication =
      noClasses()
          .that()
          .resideInAnyPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..application..");

  @ArchTest
  // RestClient in SessionData is an accepted outbound-client dependency and known technical debt.
  static final ArchRule applicationAndDomainDoNotDependOnSpringWebOrServlet =
      noClasses()
          .that()
          .resideInAnyPackage("..application..", "..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.springframework.web.servlet..",
              "org.springframework.web.bind..",
              "org.springframework.web.multipart..",
              "jakarta.servlet..");

  @ArchTest
  // Outbound port types are interfaces; data models belong elsewhere.
  static final ArchRule outputPortsAreInterfaces =
      classes()
          .that()
          .areTopLevelClasses()
          .and()
          .resideInAPackage("..application.port.out..")
          .should()
          .beInterfaces();
}
