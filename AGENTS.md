# AGENTS.md

Guidance for AI coding agents working in this repository. Humans should read `README.md`.

## Project overview

UBUMonitor Analytics is a Spring Boot REST API that serves Moodle analytics and persists the data of each Moodle site in its own encrypted SQLite database (tenant isolation).

- **Stack:** Java 25, Spring Boot 4.1.1, Maven reactor, OpenAPI delegate generation, jOOQ, Flyway, SQLite (ChaCha20 via SQLite3MultipleCiphers), Logbook, WireMock (tests).
- **Modules:**
    - `ubumonitor-analytics`: REST API, application logic, jOOQ persistence, Moodle adapters.
    - `moodle-java-client`: typed Moodle REST/AJAX client and Moodle models generated from JSON Schema.
- The API consumes the client as a module of the same reactor. The root POM declares both modules and the Spotless configuration.

## Commands

Run the narrowest check first and the full clean build last. Builds generate OpenAPI/jOOQ sources and start the Spring context, so they are slower than a single fixture.

| Goal | Command (PowerShell, from the repo root) | Cost |
|---|---|---|
| One integration fixture (run first) | `mvn -pl ubumonitor-analytics -Dtest=MoodleAnalyticsIntegrationTest '-Dintegration.fixture.pattern=classpath:/integration/public/sites/info/happy_path/test-case.json' test` | One fixture; generates sources if needed. |
| Regenerate sources | `mvn -pl ubumonitor-analytics generate-sources` (or `-pl moodle-java-client`) | Short. |
| All tests | `mvn test` | Long; includes integration tests. |
| Format | `mvn spotless:apply` | Short; may modify Java, XML and JSON files. |
| Package API and reactor dependencies | `mvn -pl ubumonitor-analytics -am package` | Long; builds the needed modules and runs their tests. |
| Run locally with the `dev` profile | `mvn -pl ubumonitor-analytics spring-boot:run '-Dspring-boot.run.profiles=dev'` | Compiles, then keeps the server in the foreground. |
| Full clean build (run last) | `mvn clean install` | Longest; cleans, generates, tests and installs the reactor. |

Notes:

- The same commands work in bash. Keep the single quotes around every `-D...`.
- Select a fixture with `integration.fixture.pattern`; replace the pattern with the desired `classpath:/integration/.../test-case.json`.
- On a clean checkout, build/install the reactor first if `moodle-java-client` is not yet in the local Maven repository.
- Spotless is bound to the `process-sources` phase, so any build also formats files.
- Recommended JVM options for deployments that use the statistics endpoint (it streams rows without a limit): `-Xms64m -Xmx256m -XX:+UseSerialGC -XX:MaxMetaspaceSize=96m -Xss512k`.

## Repository map

```text
ubumonitor-analytics/src/main/java/.../features/<feature>/
  domain/                          domain model and rules
  application/{port,service,dto}/  use cases, services and contracts
  infrastructure/{in,out}/         REST entry points and output adapters
ubumonitor-analytics/src/main/java/.../shared/
  domain/, application/, infrastructure/   cross-feature contracts and components
ubumonitor-analytics/src/main/resources/
  static/openapi/{openapi.yaml,paths,components}/   REST contract
  db/migration/                    Flyway migrations (bootstrap/tenant DB)
  application*.yml                 common configuration and profiles
ubumonitor-analytics/src/test/
  java/.../integration/            integration runner
  resources/integration/           test-case.json files and shared auth fixtures
moodle-java-client/src/main/java/      Moodle client and HTTP adapters
moodle-java-client/src/main/resources/schema/   JSON Schema for Moodle requests/responses
```

Generated code lives under `target` and must never be edited:

- OpenAPI and jOOQ: `ubumonitor-analytics/target/generated-sources`
- JSON Schema models: `moodle-java-client/target/generated-sources/jsonschema2pojo`

Edit the source instead (OpenAPI YAML, Flyway migration or JSON Schema) and regenerate.

## Architecture

Hexagonal architecture, organized by feature. Keep this flow:

`infrastructure/in` (REST delegate) → `application/port/in` (use case) → `application/service` → `application/port/out` → `infrastructure/out` (jOOQ, Moodle or other IO)

- Output adapters implement output ports. A REST delegate must not access jOOQ or the Moodle client directly.
- `domain` is independent of Spring, generated OpenAPI DTOs, Moodle models and jOOQ types. Convert types in the mappers of the corresponding adapter.
- Put code in `shared` only when its contract or behavior is shared across features (for example sessions, exceptions, common configuration). Keep feature-specific models and rules inside the feature.
- Canonical reference for the small endpoint `GET /api/public/sites/info`:
    1. `ubumonitor-analytics/src/main/resources/static/openapi/paths/sites/public-info.yaml`
    2. `features/sites/infrastructure/in/rest/SitesApiDelegateImpl.java`
    3. `features/sites/application/port/in/GetPublicSiteInfoUseCase.java`
    4. `features/sites/application/service/PublicSiteInfoService.java`
    5. `features/sites/application/port/out/PublicSiteInfoApiPort.java`
    6. `features/sites/infrastructure/out/moodle/PublicSiteInfoApiAdapter.java` and `SiteInfoApiAdapterMapper.java`
    7. `ubumonitor-analytics/src/test/resources/integration/public/sites/info/happy_path/test-case.json`

## Recipes

### Add or change an endpoint

1. Edit `ubumonitor-analytics/src/main/resources/static/openapi/paths/<resource>/<operation>.yaml` and its schemas under `components/schemas/`. Register the path in `static/openapi/openapi.yaml`.
2. Run `mvn -pl ubumonitor-analytics generate-sources`. Inspect the generated interfaces/DTOs under `target/generated-sources/openapi/`; do not edit them.
3. Implement the generated method in the feature's delegate, delegating to the use case.
4. Define or update `application/port/in/*UseCase.java` and its `application/service/*Service.java`.
5. If IO is needed, define `application/port/out/`, implement the port in `infrastructure/out/` and adapt data in a mapper.
6. Add a `test-case.json` fixture under `ubumonitor-analytics/src/test/resources/integration` and run only that fixture.

### Add a field or table

1. Add a versioned migration `ubumonitor-analytics/src/main/resources/db/migration/V<N>__<description>.sql`. Never rewrite a migration that has already been applied.
2. Run `mvn -pl ubumonitor-analytics generate-sources`: Flyway migrates `target/bootstrap.db` and jOOQ generates code from that SQLite database.
3. Update the persistence mapper, then the jOOQ adapter that reads/writes the field or table.
4. Update the integration fixture/data that is needed and validate the feature.

### Add or change a Moodle mapping

1. Edit the matching request/response in `moodle-java-client/src/main/resources/schema/<component>/...`. Respect the `title`, `$ref` and types of neighboring schemas.
2. Implement or update the API method in `moodle-java-client/src/main/java/es/ubu/lsi/moodle/api/<component>/` using the models generated under `target/generated-sources/jsonschema2pojo/`.
3. Run `mvn -pl moodle-java-client generate-sources` to verify generation.
4. In `ubumonitor-analytics`, adapt the call in the feature's Moodle adapter and convert the client model to application/domain types in the mapper.
5. Add or adjust the WireMock mock in the fixture. Never call a real Moodle.

### Add or update an integration fixture

1. Create or update `ubumonitor-analytics/src/test/resources/integration/<group>/<endpoint>/<case>/test-case.json`.
2. Include `request` and `expectedResponse`. Add `moodleMocks` when Moodle is called. For authenticated endpoints use `authFixture` (for example `classpath:/integration/shared/teacher-sso-auth.json`) or define `auth`. Add `databaseSetup` (a list of SQL statements) when initial state is needed.
3. Run the "One integration fixture" command with `integration.fixture.pattern` set to the fixture's classpath path. The runner discovers `test-case.json` files and creates an isolated database per case under `target/test-moodle-dbs/`.

## Conventions

### Code

- Mark service reads with `@Transactional(readOnly = true)` and writes with `@Transactional`, as in the services of `features/course/content` and `features/course/enrollment`.
- Prefer explicit `for` loops over lambdas and `forEach`. Use named static methods or method references instead of inline lambdas, and avoid `BiConsumer` where reasonable.
- For maps with unique keys use `Collectors.toMap`, not `groupingBy`. Define a named merger when collisions are possible.
- Check `null` with guard clauses and early returns.
- When imperative and stream forms are equally clear, prefer the imperative one; use streams when they express the transformation better. Equivalent example: `for (User user : users) names.add(user.getName());` / `users.stream().map(User::getName).toList();`.
- Use only Java 25-compatible APIs.

### API and errors

- Reuse the existing application exceptions and keep the `GlobalExceptionHandler` contract: 400 `BAD_REQUEST`, 401 `UNAUTHORIZED`, 404 `NOT_FOUND`, 409 `CONFLICT`, 500 `UNKNOWN_ERROR`. Validation errors return 400 `VALIDATION_ERROR`; unknown routes return 404 `RESOURCE_NOT_FOUND`.
- Declare constraints and validation in OpenAPI; the generator enables `useBeanValidation`. Do not patch generated DTOs.

### Security and logging

- Never log tokens, passwords, keys or sensitive headers. Keep the Logbook masking of `Authorization`, `X-Moodle-Token`, `Cookie`, `password` and `token`.

### Tests

- Cover new behavior with focused fixtures, WireMock and the isolated SQLite Cipher databases created by the runner.

## Boundaries

### Always (no need to ask)

- Read and preserve the user's existing changes; keep the diff limited to the task.
- Add focused coverage for new behavior and run the applicable checks.
- Verify integrations with fixtures, WireMock and the runner's isolated SQLite Cipher databases.

### Ask first

- Changing REST contracts, authentication/authorization or other security decisions.
- Changing tenant configuration or isolation, the database schema/migrations, or adding dependencies.

### Never

- Edit anything under `target`, or introduce secrets in code, fixtures, logs or documentation.
- Log tokens or credentials, or use a real Moodle in tests.
- Run destructive Git operations (for example `reset --hard`) or create commits without an explicit request.
- Modify existing tests or fixtures when adding new coverage is enough. Never delete or weaken assertions to make a test pass.

## Gotchas

| Symptom | Cause | Fix |
|---|---|---|
| Compilation fails with missing jOOQ types after `clean` | `flyway:migrate` and `jooq:generate` run in `generate-sources` against `target/bootstrap.db` | Run the Maven lifecycle from `generate-sources`; do not compile by hand. |
| jOOQ code cannot find a table or column | Migrations declare identifiers in uppercase (`USERS`, `COURSES`, `USERS_IMAGES`) | Keep the SQL names exactly as in the DDL. |
| A DTO change does not show up | OpenAPI/JSON Schema output is regenerated into `target` | Change the YAML/JSON Schema source and regenerate. |
| Swagger is not available locally | `application-dev.yml` is not the default profile | Activate the `dev` profile for local use only. |
| `run.bat` is slow and kills another process | It always runs `mvn clean install` and kills the listener on port 9090 | Iterate with module-targeted commands; check the port before using the `.bat`. Its H2/DBeaver section is legacy; tenant data is SQLite. |
| Build fails or recent Java APIs are missing | Both modules pin Java 25 and the API uses Spring Boot 4.1.1 | Build with JDK 25 and avoid incompatible APIs. |
| The integration test runs every fixture | The default pattern is `classpath:/integration/**/test-case.json` | Pass `-Dintegration.fixture.pattern=classpath:/integration/.../test-case.json`. |
| Unexpected formatting changes after a build | Spotless runs at `process-sources` | Expected; keep only the formatting you intend. |
| `features/users/info/appication/` looks misspelled | The package name is `appication` | Leave it; do not rename it as part of an unrelated change. |

## Definition of done

- [ ] The specific fixture passes and covers the change.
- [ ] `mvn spotless:apply` completes; review and keep only the expected formatting.
- [ ] The affected module builds: `mvn -pl ubumonitor-analytics -am package` or `mvn -pl moodle-java-client package`.
- [ ] `git diff` is clean: no generated changes under `target` and no changes outside the task.
- [ ] If the change touches shared generation or more than one module, `mvn clean install` also passes.

## Git and release

- Commit messages follow Conventional Commits, as in the existing history:
    - `refactor: clean up imports and improve assertions in integration tests`
    - `chore: prepare next development version (0.0.4-SNAPSHOT)`
    - `chore(release): v0.0.3`
- The workflow `.github/workflows/maven.yml` publishes on push to `main`, then merges the release commit into `develop` and prepares the next `-SNAPSHOT` version. Do not assume any other branching flow.