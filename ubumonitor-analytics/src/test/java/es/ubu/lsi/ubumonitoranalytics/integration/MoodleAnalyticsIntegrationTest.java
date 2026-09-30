package es.ubu.lsi.ubumonitoranalytics.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.WireMockServer;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.SessionStorePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.JooqProvider;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle.config.MoodleConfig;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MoodleAnalyticsIntegrationTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String FIXTURE_PATTERN =
      System.getProperty("integration.fixture.pattern", "classpath:/integration/**/test-case.json");
  private static final String TEST_DATABASE_PATH =
      Path.of(System.getProperty("moodle.db.base-path", "target/test-moodle-dbs"))
          .resolve("run-" + UUID.randomUUID())
          .toAbsolutePath()
          .toString();

  @LocalServerPort private int port;

  @Autowired private SessionStorePort sessionStore;

  @Autowired private JooqProvider jooqProvider;

  @Autowired private MoodleConfig moodleConfig;

  @DynamicPropertySource
  static void configureTestDatabase(DynamicPropertyRegistry registry) {
    registry.add("moodle.db.base-path", () -> TEST_DATABASE_PATH);
  }

  @TestFactory
  Stream<DynamicTest> runFixtureTests() throws IOException {
    Resource[] fixtures = new PathMatchingResourcePatternResolver().getResources(FIXTURE_PATTERN);
    assertTrue(fixtures.length > 0, "No integration test-case.json fixtures were found");

    return Arrays.stream(fixtures).flatMap(this::dynamicTests);
  }

  private Stream<DynamicTest> dynamicTests(Resource fixture) {
    try {
      JsonNode testCase = OBJECT_MAPPER.readTree(fixture.getInputStream());
      if (!testCase.has("auth") && !testCase.has("authFixture")) {
        return Stream.of(
            DynamicTest.dynamicTest(fixtureName(fixture), () -> runEndpointCase(testCase)));
      }
      return Stream.of(DynamicTest.dynamicTest(fixtureName(fixture), () -> runFixture(fixture)));
    } catch (IOException exception) {
      throw new RuntimeException(
          "Could not read integration fixture " + fixture.getFilename(), exception);
    }
  }

  private void runEndpointCase(JsonNode endpointCase) throws IOException {
    JsonNode request = endpointCase.path("request");
    String previousDatabasePath = configureFixtureDatabase(endpointCase.path("name").asText());
    WireMockServer wireMock = new WireMockServer(0);
    wireMock.start();
    String accessToken = null;
    try {
      JsonNode moodleMocks = endpointCase.path("moodleMocks");
      moodleMocks.forEach(mock -> configureMock(wireMock, mock));
      String requestPath =
          request.path("path").asText().replace("${MOODLE_HOST}", wireMock.baseUrl());
      String body = requestBody(request.path("body"));
      if (body != null) {
        body = body.replace("${MOODLE_HOST}", wireMock.baseUrl());
      }
      ResponseEntity<String> response = exchangeRequest(request, requestPath, null, body);
      assertEquals(
          endpointCase.path("expectedResponse").path("status").asInt(),
          response.getStatusCode().value(),
          "Unexpected status for " + requestPath + ": " + response.getBody());
      assertExpectedBody(endpointCase, response);
      MediaType contentType = response.getHeaders().getContentType();
      if (contentType != null && MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
        JsonNode tokenResponse = OBJECT_MAPPER.readTree(response.getBody());
        accessToken = tokenResponse.path("accessToken").asText(null);
      }
    } finally {
      if (accessToken != null && !accessToken.isBlank()) {
        SessionData sessionData = sessionStore.getSession(accessToken);
        if (sessionData != null) {
          jooqProvider.closeTenant(sessionData);
        }
        sessionStore.invalidateSession(accessToken);
      }
      moodleConfig.getDb().setBasePath(previousDatabasePath);
      wireMock.stop();
    }
  }

  private void runFixture(Resource fixture) throws Exception {
    JsonNode testCase = OBJECT_MAPPER.readTree(fixture.getInputStream());
    String previousDatabasePath = configureFixtureDatabase(fixtureName(fixture));
    WireMockServer wireMock = new WireMockServer(0);
    wireMock.start();

    String accessToken = null;

    try {
      JsonNode databaseSetup = testCase.path("databaseSetup");
      JsonNode authDefinition = testCase.path("auth");
      if (authDefinition.isMissingNode()) {
        String authFixturePath = testCase.path("authFixture").asText();
        Resource sharedAuthFixture =
            new PathMatchingResourcePatternResolver().getResource(authFixturePath);
        JsonNode sharedAuthFixtureJson = OBJECT_MAPPER.readTree(sharedAuthFixture.getInputStream());
        authDefinition = sharedAuthFixtureJson.path("auth");
        sharedAuthFixtureJson.path("moodleMocks").forEach(mock -> configureMock(wireMock, mock));
        if (databaseSetup.isMissingNode() || (databaseSetup.isArray() && databaseSetup.isEmpty())) {
          databaseSetup = sharedAuthFixtureJson.path("databaseSetup");
        }
      }
      testCase.path("moodleMocks").forEach(mock -> configureMock(wireMock, mock));
      JsonNode authRequest = authDefinition.path("request");
      ObjectNode authBody = authRequest.path("body").deepCopy();
      authBody.put("host", wireMock.baseUrl());
      ResponseEntity<String> authResponse =
          exchange(
              authRequest.path("method").asText("POST"),
              authRequest.path("path").asText(),
              authBody.toString(),
              OBJECT_MAPPER.createObjectNode(),
              null);
      assertEquals(
          authDefinition.path("expectedResponse").path("status").asInt(200),
          authResponse.getStatusCode().value());
      accessToken = OBJECT_MAPPER.readTree(authResponse.getBody()).path("accessToken").asText();
      assertFalse(accessToken.isBlank(), "Auth response must include an access token");
      ObjectNode expectedAuthBody = authDefinition.path("expectedResponse").path("body").deepCopy();
      expectedAuthBody.put("accessToken", accessToken);
      assertEquals(expectedAuthBody, OBJECT_MAPPER.readTree(authResponse.getBody()));
      applyDatabaseSetup(databaseSetup, wireMock.baseUrl(), authBody);

      JsonNode request = testCase.path("request");
      String requestPath =
          request.path("path").asText().replace("${MOODLE_HOST}", wireMock.baseUrl());
      String requestBody = requestBody(request.path("body"));
      if (requestBody != null) {
        requestBody = requestBody.replace("${MOODLE_HOST}", wireMock.baseUrl());
      }
      ResponseEntity<String> response =
          exchangeRequest(request, requestPath, accessToken, requestBody);
      assertEquals(
          testCase.path("expectedResponse").path("status").asInt(200),
          response.getStatusCode().value(),
          "Unexpected status for " + request.path("path").asText() + ": " + response.getBody());

      assertExpectedBody(testCase, response);
    } finally {
      if (accessToken != null && !accessToken.isBlank()) {
        SessionData sessionData = sessionStore.getSession(accessToken);
        if (sessionData != null) {
          jooqProvider.closeTenant(sessionData);
        }
        sessionStore.invalidateSession(accessToken);
      }
      moodleConfig.getDb().setBasePath(previousDatabasePath);
      wireMock.stop();
    }
  }

  private String configureFixtureDatabase(String fixtureName) {
    String previousPath = moodleConfig.getDb().getBasePath();
    String databaseDirectory =
        Path.of(TEST_DATABASE_PATH)
            .resolve(fixtureName.replace('/', '_') + "-" + UUID.randomUUID())
            .toString();
    moodleConfig.getDb().setBasePath(databaseDirectory);
    return previousPath;
  }

  private void applyDatabaseSetup(JsonNode setup, String host, ObjectNode authBody) {
    if (!setup.isArray()) {
      return;
    }
    String username = authBody.path("username").asText("teacher");
    SessionData sessionData =
        SessionData.builder()
            .host(URI.create(host))
            .username(username)
            .dbPassword(authBody.path("dbPassword").asText())
            .build();
    var dsl = jooqProvider.getDSLContext(sessionData);
    setup.forEach(statement -> dsl.execute(statement.asText().replace("${MOODLE_HOST}", host)));
  }

  private void assertExpectedBody(JsonNode testCase, ResponseEntity<String> response)
      throws IOException {
    JsonNode expectedBody = testCase.path("expectedResponse").path("body");
    if (!expectedBody.isMissingNode()) {
      JsonNode actualBody = OBJECT_MAPPER.readTree(response.getBody());
      if (expectedBody.isObject() && "${JWT}".equals(expectedBody.path("accessToken").asText())) {
        ObjectNode resolvedExpectedBody = expectedBody.deepCopy();
        String actualToken = actualBody.path("accessToken").asText();
        assertFalse(actualToken.isBlank(), "Response must include an access token");
        resolvedExpectedBody.put("accessToken", actualToken);
        expectedBody = resolvedExpectedBody;
      }
      assertEquals(expectedBody, actualBody);
    }
  }

  private ResponseEntity<String> exchange(
      String method, String path, String body, JsonNode requestHeaders, String jwtToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    requestHeaders
        .properties()
        .forEach(
            header -> {
              String value = header.getValue().asText();
              if (jwtToken != null) {
                value =
                    value.equals("******")
                        ? "Bearer " + jwtToken
                        : value.replace("${JWT}", jwtToken);
              }
              headers.set(header.getKey(), value);
            });
    RestClient.RequestBodySpec request =
        RestClient.create("http://localhost:" + port)
            .method(HttpMethod.valueOf(method))
            .uri(path)
            .headers(requestHeadersBuilder -> requestHeadersBuilder.addAll(headers));
    if (body != null) {
      request.body(body);
    }
    return request
        .retrieve()
        .onStatus(HttpStatusCode::isError, (_, _) -> {})
        .toEntity(String.class);
  }

  private ResponseEntity<String> exchangeRequest(
      JsonNode endpointRequest, String path, String jwtToken, String body) {
    JsonNode multipart = endpointRequest.path("multipart");
    if (!multipart.isObject()) {
      return exchange(
          endpointRequest.path("method").asText("GET"),
          path,
          body,
          endpointRequest.path("headers"),
          jwtToken);
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    applyHeaders(headers, endpointRequest.path("headers"), jwtToken);

    HttpHeaders fileHeaders = new HttpHeaders();
    fileHeaders.setContentType(
        MediaType.parseMediaType(multipart.path("contentType").asText("text/plain")));
    ByteArrayResource file =
        new ByteArrayResource(multipart.path("content").asText().getBytes(StandardCharsets.UTF_8)) {
          @Override
          public String getFilename() {
            return multipart.path("filename").asText("upload.txt");
          }
        };
    MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
    parts.add(multipart.path("field").asText("logFile"), new HttpEntity<>(file, fileHeaders));

    return RestClient.create("http://localhost:" + port)
        .method(HttpMethod.valueOf(endpointRequest.path("method").asText("POST")))
        .uri(path)
        .headers(requestHeaders -> requestHeaders.addAll(headers))
        .body(parts)
        .retrieve()
        .onStatus(HttpStatusCode::isError, (_, _) -> {})
        .toEntity(String.class);
  }

  private void applyHeaders(HttpHeaders headers, JsonNode requestHeaders, String jwtToken) {
    requestHeaders
        .properties()
        .forEach(
            header -> {
              String value = header.getValue().asText();
              if (jwtToken != null) {
                value =
                    value.equals("******")
                        ? "Bearer " + jwtToken
                        : value.replace("${JWT}", jwtToken);
              }
              headers.set(header.getKey(), value);
            });
  }

  private void configureMock(WireMockServer wireMock, JsonNode mockDefinition) {
    JsonNode requestDefinition = mockDefinition.path("request");
    var mapping =
        request(
            requestDefinition.path("method").asText("POST"),
            urlEqualTo(requestDefinition.path("path").asText()));

    JsonNode body = requestDefinition.path("body");
    if (!body.isMissingNode()) {
      if (body.isObject() && body.has("contains")) {
        mapping.withRequestBody(containing(body.path("contains").asText()));
      } else {
        mapping.withRequestBody(
            body.isTextual() ? equalTo(body.asText()) : equalToJson(body.toString(), false, false));
      }
    }

    JsonNode response = mockDefinition.path("response");
    wireMock.stubFor(
        mapping.willReturn(
            aResponse()
                .withStatus(response.path("status").asInt(200))
                .withHeader("Content-Type", response.path("contentType").asText("application/json"))
                .withBody(
                    response.path("body").isTextual()
                        ? response.path("body").asText()
                        : response.path("body").toString())));
  }

  private String requestBody(JsonNode body) {
    if (body.isMissingNode() || body.isNull()) {
      return null;
    }
    return body.isTextual() ? body.asText() : body.toString();
  }

  private String fixtureName(Resource fixture) {
    try {
      Path integrationRoot = fixture.getFile().toPath().getParent();
      while (integrationRoot != null
          && !"integration".equals(integrationRoot.getFileName().toString())) {
        integrationRoot = integrationRoot.getParent();
      }
      return integrationRoot == null
          ? fixture.getFilename()
          : integrationRoot
              .relativize(fixture.getFile().toPath().getParent())
              .toString()
              .replace('\\', '/');
    } catch (IOException exception) {
      return fixture.getFilename();
    }
  }
}
