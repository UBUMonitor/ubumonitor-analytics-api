package es.ubu.lsi.ubumonitoranalytics.features.users.info.infrastructure.out.moodle;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Sites.SITES;

import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.out.ServerTimeZonePort;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import java.time.ZoneId;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class ServerTimeZoneAdapter implements ServerTimeZonePort {

  private final CurrentSessionContext currentSessionContext;
  private final Jooq jooq;

  @Override
  public ZoneId getServerTimezone() {

    Long userId = currentSessionContext.getSessionData().getUserId();

    ZoneId timeZone =
        jooq.dsl()
            .select(SITES.TIME_ZONE)
            .from(SITES)
            .where(SITES.USER_ID.eq(userId))
            .fetchOptional(SITES.TIME_ZONE)
            .map(ZoneId::of)
            .orElse(null);

    if (timeZone == null) {
      timeZone = getTimeZoneFromProfile();
      jooq.dsl()
          .update(SITES)
          .set(SITES.TIME_ZONE, timeZone.getId())
          .where(SITES.USER_ID.eq(userId))
          .execute();
    }
    return timeZone;
  }

  private ZoneId getTimeZoneFromProfile() {
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
