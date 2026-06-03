package es.ubu.lsi.ubumonitoranalytics.features.courselogs.infrastructure.out.moodle;

import es.ubu.lsi.ubumonitoranalytics.features.courselogs.application.port.out.MoodlePort;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.jsoup.Jsoup;
import org.springframework.http.MediaType;

import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

@Component
@Slf4j
public class MoodleAdapter implements MoodlePort {
    @Override
    public RestClient login(String username, String password, URI host) {

        RestClient client = RestClient.builder()
            .baseUrl(host)
            .build();

        String loginToken = getLoginToken(client);
        // 2. login
        postLogin(username, password, loginToken, client);

        return client;
    }

    private static void postLogin(String username, String password, String loginToken, RestClient client) {

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", username);
        form.add("password", password);
        form.add("logintoken", loginToken);

        client.post()
            .uri("/login/index.php")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(form)
            .retrieve()
            .toBodilessEntity();
    }

    private String getLoginToken(RestClient client) {

        String html = client.get()
            .uri("/login/index.php")
            .retrieve()
            .body(String.class);


        assert html != null;
        return Jsoup.parse(html)
            .select("input[name=logintoken]")
            .attr("value");
    }
    @Override
    public void downloadAllLogs(Integer courseId, RestClient restClient, Consumer<CSVRecord> consumer) {

        restClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/report/log/index.php")
                .queryParam("download", "csv")
                .queryParam("id", courseId)
                .queryParam("chooselog", "1")
                .queryParam("logreader", "logstore_standard")
                .queryParam("lang", "en")
                .build())
            .exchange((request, response) -> {

                processCSV(consumer, response.getBody());

                return null;
            });
    }




    @Override
    public void downloadPartialLogs(Integer courseId, long since, RestClient restClient,  Consumer<CSVRecord> consumer) {
         restClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/report/log/index.php")
                .queryParam("download", "csv")
                .queryParam("id", courseId)
                .queryParam("chooselog", "1")
                .queryParam("logreader", "logstore_standard")
                .queryParam("date", since)
                .queryParam("lang", "en")
                .build())
             .exchange((request, response) -> {

                 processCSV(consumer, response.getBody());

                 return null;
             });
    }

    private void processCSV(Consumer<CSVRecord> consumer, InputStream inputStream) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
            .setHeader() // 👈 usa la primera fila como header real
            .setSkipHeaderRecord(true)
            .setIgnoreSurroundingSpaces(true)
            .setTrim(true)
            .get();

        try (InputStream is = inputStream;
             BufferedReader reader = buildReader(is);
             CSVParser parser = format.parse(reader)) {
            for (CSVRecord csvRecord : parser) {
                consumer.accept(csvRecord);
            }
        }
    }



    @SneakyThrows
    private BufferedReader buildReader(InputStream inputStream) {

        InputStream bomInputStream = BOMInputStream.builder()
            .setInputStream(inputStream)
            .get();

        return new BufferedReader(
            new InputStreamReader(bomInputStream, StandardCharsets.UTF_8),
            64 * 1024
        );
    }
}
