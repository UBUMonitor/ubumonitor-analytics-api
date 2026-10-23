package es.ubu.lsi.ubumonitoranalytics.features.sites.infrastructure.out.moodle;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.core.webservice.getsiteinfo.request.GetSiteInfoRequestApi;
import es.ubu.lsi.moodle.model.core.webservice.getsiteinfo.response.GetSiteInfoResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.SiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.SiteInfoApiPort;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.concurrent.AsyncOperations;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Fetches authenticated site and user information from Moodle. */
@Component
@RequiredArgsConstructor
public class SiteInfoApiAdapter implements SiteInfoApiPort {

  private final Client client;
  private final SiteInfoApiAdapterMapper siteInfoApiAdapterMapper;
  private final AsyncOperations asyncOperations;
  private final CurrentSessionContext currentSessionContext;

  @Override
  public SiteInfo fetchSiteInfo() {

    CompletableFuture<GetSiteInfoResponseApi> call1 =
        asyncOperations.supplyAsync(
            () -> client.coreWebservice().getSiteInfo(new GetSiteInfoRequestApi()));

    CompletableFuture<ZoneId> call2 = asyncOperations.supplyAsync(this::getTimezone);

    return siteInfoApiAdapterMapper.toDomain(call1.join(), call2.join());
  }

  private ZoneId getTimezone() {
    RestClient moodleSession = currentSessionContext.getSessionData().getRestClient();
    String html =
        moodleSession
            .get()
            .uri(
                uriBuilder -> uriBuilder.path("/user/profile.php").queryParam("lang", "en").build())
            .header("Accept-Language", "en")
            .retrieve()
            .body(String.class);

    if (html == null || html.isEmpty()) {
      return ZoneOffset.UTC;
    }
    Document doc = Jsoup.parse(html);
    for (Element dl : doc.select("li.contentnode > dl")) {
      Element dt = dl.selectFirst("> dt");
      Element dd = dl.selectFirst("> dd");
      if (dt != null && dd != null && dt.text().trim().equals("Timezone")) {
        return ZoneId.of(dd.text().trim());
      }
    }
    // Respaldo: valor ya resuelto por Moodle en M.cfg
    for (Element script : doc.select("script")) {
      var m =
          java.util.regex.Pattern.compile("\"usertimezone\"\\s*:\\s*\"([^\"]+)\"")
              .matcher(script.data());
      if (m.find()) {
        return ZoneId.of(m.group(1));
      }
    }
    return ZoneOffset.UTC;
  }
}
