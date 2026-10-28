package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.out.moodle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.ubu.lsi.ubumonitoranalytics.features.system.database.domain.model.DatabaseInfo;
import es.ubu.lsi.ubumonitoranalytics.features.system.database.infrastructure.out.DatabaseListAdapter;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.moodle.config.MoodleConfig;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseListAdapterTest {

  @TempDir Path tempDir;

  @Test
  void groupsDatabaseFilesByHostAndOmitsTheExtensionFromTheirIds() throws Exception {
    Files.createFile(tempDir.resolve("https~school.moodledemo.net~student.mv.db"));
    Files.createFile(tempDir.resolve("https~school.moodledemo.net~teacher.mv.db"));
    Files.createFile(tempDir.resolve("http~school.moodledemo.net~student.mv.db"));
    MoodleConfig moodleConfig =
        new MoodleConfig(
            new MoodleConfig.Db(tempDir.toString(), null), new MoodleConfig.Api(null, null));
    DatabaseListAdapter adapter = new DatabaseListAdapter(moodleConfig);

    var hosts = adapter.getDatabaseList();

    assertEquals(2, hosts.size());
    var httpsHost =
        hosts.stream()
            .filter(host -> host.getHost().equals(URI.create("https://school.moodledemo.net")))
            .findFirst()
            .orElseThrow();
    assertEquals(2, httpsHost.getDatabases().size());
    assertEquals(
        List.of("https~school.moodledemo.net~student", "https~school.moodledemo.net~teacher"),
        httpsHost.getDatabases().stream().map(DatabaseInfo::getId).sorted().toList());
    assertEquals(
        List.of("student", "teacher"),
        httpsHost.getDatabases().stream().map(DatabaseInfo::getUserName).sorted().toList());

    var httpHost =
        hosts.stream()
            .filter(host -> host.getHost().equals(URI.create("http://school.moodledemo.net")))
            .findFirst()
            .orElseThrow();
    assertEquals(1, httpHost.getDatabases().size());
  }
}
