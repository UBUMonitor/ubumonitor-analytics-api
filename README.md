# UBUMonitor Analytics

UBUMonitor Analytics is a Spring Boot API that provides Moodle analytics over REST and stores tenant data in isolated H2 databases. The application follows a hexagonal architecture and uses OpenAPI delegate generation, jOOQ, Flyway, and a dedicated Moodle Java client.

## Requirements

- JDK 25
- Maven

## Build

```text
mvn clean install
```

## Tests

Run the full test suite with:

```text
mvn test
```

To run one integration fixture, provide its classpath pattern:

```text
mvn -pl ubumonitor-analytics -Dtest=MoodleAnalyticsIntegrationTest '-Dintegration.fixture.pattern=classpath:/integration/<path>/test-case.json' test
```

## Development profile

Start the API with the local development profile:

```text
mvn -pl ubumonitor-analytics spring-boot:run '-Dspring-boot.run.profiles=dev'
```

## Project structure

- `ubumonitor-analytics`: REST API, application services, tenant persistence, and Moodle adapters.
- `moodle-java-client`: typed Moodle REST and AJAX client.
- `ubumonitor-analytics/src/main/resources/static/openapi`: OpenAPI source files.
- `ubumonitor-analytics/src/main/resources/db/migration`: Flyway migrations for tenant databases.
- `ubumonitor-analytics/src/test/resources/integration`: fixture-driven integration tests.

Generated sources under `target/` must not be edited.

## Adding an endpoint

1. Add or update the path and schemas under `ubumonitor-analytics/src/main/resources/static/openapi`.
2. Register the path in `openapi.yaml` and run `mvn -pl ubumonitor-analytics generate-sources`.
3. Implement the generated delegate by calling an input port.
4. Add the application service and output ports required by the use case.
5. Implement infrastructure adapters and mappers at the output boundary.
6. Add a focused integration fixture and run it before the module build.
