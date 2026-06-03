package es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out;

import org.apache.commons.csv.CSVRecord;
import org.springframework.web.client.RestClient;


import java.net.URI;
import java.util.function.Consumer;


public interface MoodlePort {

    RestClient login(String username, String password, URI host);
    void downloadAllLogs(Integer courseId, RestClient restClient, Consumer<CSVRecord> consumer);
    void downloadPartialLogs(Integer courseId, long since, RestClient restClient, Consumer<CSVRecord> consumer);
}
