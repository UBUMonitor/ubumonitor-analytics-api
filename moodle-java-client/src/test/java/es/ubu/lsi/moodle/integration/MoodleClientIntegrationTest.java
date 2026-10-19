package es.ubu.lsi.moodle.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.core.DefaultClient;
import es.ubu.lsi.moodle.http.JavaHttpTransport;
import es.ubu.lsi.moodle.json.JacksonMapper;
import es.ubu.lsi.moodle.model.login.token.request.LoginTokenRequestApi;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

class MoodleClientIntegrationTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @TestFactory
  Stream<DynamicTest> runFixtureTests() throws Exception {
    Path fixtureDirectory =
        Path.of(
            Objects.requireNonNull(getClass().getClassLoader().getResource("integration")).toURI());
    List<Path> fixtures;
    try (Stream<Path> paths = Files.walk(fixtureDirectory)) {
      fixtures =
          paths
              .filter(path -> path.getFileName().toString().equals("test-case.json"))
              .sorted()
              .toList();
    }
    assertFalse(fixtures.isEmpty(), "No integration test-case.json fixtures were found");

    return fixtures.stream()
        .map(
            fixture -> {
              String fixtureName =
                  fixtureDirectory.relativize(fixture).toString().replace('\\', '/');
              return DynamicTest.dynamicTest(
                  fixtureName,
                  () -> {
                    try {
                      runFixture(fixture);
                    } catch (Exception exception) {
                      throw new AssertionError(
                          "Integration fixture failed: " + fixtureName, exception);
                    }
                  });
            });
  }

  private void runFixture(Path fixture) throws Exception {
    JsonNode testCase = OBJECT_MAPPER.readTree(fixture.toFile());
    WireMockServer wireMock = new WireMockServer(0);
    wireMock.start();

    try {
      JsonNode auth = testCase.path("auth").path("request");
      testCase
          .path("moodleMocks")
          .forEach(mock -> configureMock(wireMock, mock, wireMock.baseUrl()));

      ObjectNode loginRequestJson = OBJECT_MAPPER.treeToValue(auth.path("body"), ObjectNode.class);
      loginRequestJson.put("baseurl", wireMock.baseUrl());
      LoginTokenRequestApi loginRequest =
          OBJECT_MAPPER.treeToValue(loginRequestJson, LoginTokenRequestApi.class);
      Client loginClient =
          new DefaultClient(
              new JavaHttpTransport(new JacksonMapper(), URI.create(wireMock.baseUrl()), ""));
      JsonNode loginResponse = OBJECT_MAPPER.valueToTree(loginClient.login(loginRequest));
      assertEquals(testCase.path("auth").path("expectedResponse").path("body"), loginResponse);
      String token = loginResponse.path("token").asString();
      assertTrue(token != null && !token.isBlank(), "Login mock must return a token");

      Client client =
          new DefaultClient(
              new JavaHttpTransport(new JacksonMapper(), URI.create(wireMock.baseUrl()), token));
      JsonNode requestDefinition = testCase.path("request");
      assertEquals("CLIENT", requestDefinition.path("method").asString());
      String[] operationPath = requestDefinition.path("path").asString().split("\\.", 2);
      Object api = Client.class.getMethod(operationPath[0]).invoke(client);
      Class<?> apiInterface = api.getClass().getInterfaces()[0];
      Method operation =
          java.util.stream.Stream.of(apiInterface.getMethods())
              .filter(method -> method.getName().equals(operationPath[1]))
              .findFirst()
              .orElseThrow();
      Object apiRequest =
          OBJECT_MAPPER.treeToValue(
              requestDefinition.path("body"), operation.getParameterTypes()[0]);
      JsonNode response = OBJECT_MAPPER.valueToTree(operation.invoke(api, apiRequest));
      assertJsonEquals(testCase.path("expectedResponse").path("body"), response, "$");
      assertTrue(
          wireMock.getAllServeEvents().stream()
              .allMatch(
                  event ->
                      event.getResponse().getStatus()
                          == testCase.path("expectedResponse").path("status").asInt()),
          "Unexpected HTTP status from Moodle mock");
    } finally {
      wireMock.stop();
    }
  }

  private void assertJsonEquals(JsonNode expected, JsonNode actual, String path) {
    if (expected.isNumber() && actual.isNumber()) {
      assertEquals(
          0,
          expected.decimalValue().compareTo(actual.decimalValue()),
          "Unexpected number at " + path + ": expected " + expected + " but was " + actual);
      return;
    }

    if (expected.isObject()) {
      assertTrue(actual.isObject(), "Expected an object at " + path + " but was " + actual);
      Set<String> expectedFields = new HashSet<>();
      expected.properties().forEach(entry -> expectedFields.add(entry.getKey()));
      Set<String> actualFields = new HashSet<>();
      actual.properties().forEach(entry -> actualFields.add(entry.getKey()));
      assertEquals(expectedFields, actualFields, "Unexpected object fields at " + path);
      expected
          .properties()
          .forEach(
              entry ->
                  assertJsonEquals(
                      entry.getValue(), actual.get(entry.getKey()), path + "." + entry.getKey()));
      return;
    }

    if (expected.isArray()) {
      assertTrue(actual.isArray(), "Expected an array at " + path + " but was " + actual);
      assertEquals(expected.size(), actual.size(), "Unexpected array length at " + path);
      for (int index = 0; index < expected.size(); index++) {
        assertJsonEquals(expected.get(index), actual.get(index), path + "[" + index + "]");
      }
      return;
    }

    assertEquals(expected, actual, "Unexpected value at " + path);
  }

  private void configureMock(WireMockServer wireMock, JsonNode mockDefinition, String moodleHost) {
    JsonNode requestDefinition = mockDefinition.path("request");
    var mapping =
        request(
            requestDefinition.path("method").asString("POST"),
            urlEqualTo(requestDefinition.path("path").asString()));
    JsonNode body = requestDefinition.path("body");
    assertFalse(body.isMissingNode(), "Every Moodle mock request must define a body");
    if (body.isString()) {
      String expectedBody =
          body.asString()
              .replace("${MOODLE_HOST}", URLEncoder.encode(moodleHost, StandardCharsets.UTF_8));
      for (String parameter : expectedBody.split("&")) {
        int separator = parameter.indexOf('=');
        assertTrue(separator > 0, "Form body parameters must use key=value format");
        String name = URLDecoder.decode(parameter.substring(0, separator), StandardCharsets.UTF_8);
        String value =
            URLDecoder.decode(parameter.substring(separator + 1), StandardCharsets.UTF_8);
        mapping.withFormParam(name, equalTo(value));
      }
    } else {
      mapping.withRequestBody(equalToJson(body.toString(), false, false));
    }

    JsonNode response = mockDefinition.path("response");
    wireMock.stubFor(
        mapping.willReturn(
            aResponse()
                .withStatus(response.path("status").asInt(200))
                .withHeader("Content-Type", "application/json")
                .withBody(response.path("body").toString())));
  }
}
