# AGENTS.md

Guidance for AI coding agents. Humans: see `README.md`.

## Project

UBUMonitor Analytics: Spring Boot 4.1.1 / Java 25 REST API for Moodle analytics. Each Moodle site is a tenant with its own encrypted SQLite database. Stack: OpenAPI delegates, jOOQ, Flyway, Logbook, WireMock (tests).
Maven reactor: `ubumonitor-analytics` (API, logic, persistence, Moodle adapters) and `moodle-java-client` (Moodle REST client, models generated from JSON Schema).

## Commands

Run the narrowest check first; the full clean build last. Keep single quotes around every `-D...`. Add `-B -ntp` for compact output. Spotless is bound to `process-sources`, so builds may reformat files; ignore formatting-only diffs.

| Goal | Command (repo root) |
|---|---|
| One fixture (first) | `mvn -pl ubumonitor-analytics -Dtest=MoodleAnalyticsIntegrationTest '-Dintegration.fixture.pattern=classpath:/integration/<path>/test-case.json' test` |
| Regenerate sources | `mvn -pl ubumonitor-analytics generate-sources` (or `-pl moodle-java-client`) |
| Format (the maintainer runs it; agents do not) | `mvn spotless:apply` |
| Module build | `mvn -pl ubumonitor-analytics -am package` / `mvn -pl moodle-java-client package` |
| All tests | `mvn test` |
| Full clean build (last) | `mvn clean install` |
| Run locally | `mvn -pl ubumonitor-analytics spring-boot:run '-Dspring-boot.run.profiles=dev'` |

On a clean checkout, build/install `moodle-java-client` first if it is not in the local Maven repository.

## Layout

```text
ubumonitor-analytics/src/main/java/.../features/<feature>/{domain,application/{port,service,dto},infrastructure/{in,out}}
ubumonitor-analytics/src/main/java/.../shared/            cross-feature code only
ubumonitor-analytics/src/main/resources/static/openapi/   REST contract (openapi.yaml, paths/, components/)
ubumonitor-analytics/src/main/resources/db/migration/     Flyway migrations
ubumonitor-analytics/src/test/resources/integration/      test-case.json fixtures + shared auth
moodle-java-client/src/main/resources/schema/             Moodle JSON Schemas
```

Never edit generated code under `target/` (OpenAPI/jOOQ in `ubumonitor-analytics/target/generated-sources/`, JSON Schema models in `moodle-java-client/target/generated-sources/jsonschema2pojo/`). Edit the source (YAML, migration, schema) and regenerate.

## Architecture

Hexagonal, by feature: `infrastructure/in` (REST delegate) → `application/port/in` → `application/service` → `application/port/out` → `infrastructure/out` (jOOQ, Moodle, IO).

- Adapters implement output ports; a delegate never touches jOOQ or the Moodle client.
- `domain` has no Spring, generated OpenAPI DTOs, Moodle models or jOOQ types; convert in the adapter's mapper.
- Use `shared` only for contracts shared across features.
- Reference slice (`GET /api/public/sites/info`): `openapi/paths/sites/public-info.yaml` → `features/sites/` (`SitesApiDelegateImpl`, `GetPublicSiteInfoUseCase`, `PublicSiteInfoService`, `PublicSiteInfoApiPort`, `PublicSiteInfoApiAdapter` + `SiteInfoApiAdapterMapper`) → `integration/public/sites/info/happy_path/test-case.json`.

## Recipes

### Add or change an endpoint
1. Edit `openapi/paths/<resource>/<op>.yaml` and `components/schemas/`; register the path in `openapi.yaml`.
2. `generate-sources`; inspect the generated interfaces under `target/generated-sources/openapi/`.
3. Implement the generated method in the feature delegate, calling the use case (`port/in` + `service`).
4. If IO is needed: define `port/out`, implement it in `infrastructure/out`, map data in a mapper.
5. Add a fixture and run only that fixture.

### Add a field or table
1. New migration `db/migration/V<N>__<description>.sql`; never rewrite an applied one.
2. `generate-sources` (Flyway migrates `target/bootstrap.db`, jOOQ generates from it).
3. Update the persistence mapper, then the jOOQ adapter; update fixture data; validate.

### Add or change a Moodle mapping
1. Edit the request/response in `moodle-java-client/src/main/resources/schema/<component>/` (respect `title`, `$ref`, neighboring types).
2. Update the API method in `moodle-java-client/.../moodle/api/<component>/`; run `-pl moodle-java-client generate-sources`.
3. Adapt the feature's Moodle adapter and mapper in `ubumonitor-analytics`; add/adjust the WireMock mock in the fixture.

### Add or update an integration fixture
1. `integration/<group>/<endpoint>/<case>/test-case.json` with `request` and `expectedResponse`, or an `endpoints` list of `{request, expectedResponse}` for multi-request flows; add `moodleMocks` when Moodle is called, `authFixture` (e.g. `classpath:/integration/shared/teacher-sso-auth.json`) or `auth` for authenticated endpoints, and `databaseSetup` (SQL list) for initial state.
2. Run the one-fixture command. The runner makes an isolated DB per case under `target/test-moodle-dbs/`.

## Conventions

- Service reads: `@Transactional(readOnly = true)`; writes: `@Transactional` (see `features/course/content`, `features/course/enrollment`).
- Reuse existing application exceptions; keep the `GlobalExceptionHandler` contract: 400 `BAD_REQUEST`, 401 `UNAUTHORIZED`, 404 `NOT_FOUND`, 409 `CONFLICT`, 500 `UNKNOWN_ERROR`; validation 400 `VALIDATION_ERROR`; unknown route 404 `RESOURCE_NOT_FOUND`.
- Declare validation in OpenAPI (`useBeanValidation`); never patch generated DTOs.
- Never log tokens, passwords, keys or sensitive headers; keep Logbook masking of `Authorization`, `X-Moodle-Token`, `Cookie`, `password`, `token`.
- Guard clauses and early returns for `null`.
- Prefer explicit `for` loops to lambdas/`forEach`; named static methods or method references over inline lambdas; streams only when clearer. Unique-key maps: `Collectors.toMap` (named merger on collisions), not `groupingBy`.
- Java 25 APIs only.

## Boundaries

**Ask first:** REST contract changes; authentication/authorization or other security decisions; tenant configuration/isolation; DB schema/migrations; new dependencies.

**Never:** edit `target/`; put secrets in code, fixtures, logs or docs; use a real Moodle in tests; run destructive Git (e.g. `reset --hard`) or commit without an explicit request; modify existing tests/fixtures when new coverage is enough, or delete/weaken assertions to pass a test.

**Always:** preserve the user's changes, keep the diff to the task, add focused coverage, run the applicable checks.

## Gotchas

- Missing jOOQ types after `clean`: run the lifecycle from `generate-sources`; do not compile by hand.
- jOOQ cannot find a table/column: DDL identifiers are uppercase (`USERS`, `COURSES`, `USERS_IMAGES`); keep them.
- A DTO change does not appear: regenerate from the YAML/JSON Schema source.
- Swagger only works with the `dev` profile (not the default); use it for local runs only.
- `run.bat` always runs `mvn clean install` and kills the listener on port 9090; iterate with module commands instead. Its H2/DBeaver output is legacy (data is SQLite).
- The integration test runs all fixtures by default (`classpath:/integration/**/test-case.json`); pass `-Dintegration.fixture.pattern=...` to narrow it.
- `features/users/info/appication/` is misspelled; leave it unless asked to rename.

## Done

- [ ] The specific fixture passes and covers the change.
- [ ] Affected module builds (`mvn -pl ubumonitor-analytics -am package` or `mvn -pl moodle-java-client package`).
- [ ] `git diff` has nothing under `target/` and nothing outside the task.
- [ ] If shared generation or several modules changed: `mvn clean install` passes.

## Git

Conventional Commits, e.g. `refactor: clean up imports and improve assertions in integration tests`, `chore(release): v0.0.3`. `.github/workflows/maven.yml` publishes on push to `main`, merges the release commit into `develop` and prepares the next `-SNAPSHOT`; assume no other branch flow.
