package es.ubu.lsi.ubumonitoranalytics.features.sites.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.DatabaseListPort;
import es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model.DatabaseHostInfo;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;

class DatabasesInfoServiceTest {

  @Test
  void returnsHostGroupsFromTheDatabaseListPort() {
    List<DatabaseHostInfo> expected =
        List.of(
            DatabaseHostInfo.builder()
                .host(URI.create("https://school.moodledemo.net"))
                .databases(List.of())
                .build());
    DatabaseListPort databaseListPort = () -> expected;
    DatabasesInfoService service = new DatabasesInfoService(databaseListPort);

    assertEquals(expected, service.getDatabasesInfo());
  }
}
