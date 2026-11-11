package es.ubu.lsi.ubumonitoranalytics.features.sites.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.PublicSiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.PublicSiteInfoApiPort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class PublicSiteInfoServiceTest {

  @Test
  void fetchesPublicSiteInfoWithinHostSessionScope() {
    URI host = URI.create("https://school.moodledemo.net");
    CurrentSessionContext currentSessionContext = new CurrentSessionContext();
    AtomicReference<SessionData> observedSessionData = new AtomicReference<>();
    PublicSiteInfo expected = new PublicSiteInfo();
    PublicSiteInfoApiPort siteInfoApiPort =
        () -> {
          observedSessionData.set(currentSessionContext.getSessionData());
          return expected;
        };
    PublicSiteInfoService service =
        new PublicSiteInfoService(siteInfoApiPort, currentSessionContext);

    PublicSiteInfo result = service.getPublicSiteInfo(host);

    assertSame(expected, result);
    assertEquals(host, observedSessionData.get().getHost());
    assertNull(currentSessionContext.getSessionData());
  }
}
