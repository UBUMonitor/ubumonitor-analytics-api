package es.ubu.lsi.ubumonitoranalytics.features.sites.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto.PublicSiteInfo;
import es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out.PublicSiteInfoApiPort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import java.net.URI;
import org.junit.jupiter.api.Test;

class PublicSiteInfoServiceTest {

  @Test
  void passesHostSessionDataToPublicSiteInfoPort() {
    URI host = URI.create("https://school.moodledemo.net");
    SessionData expectedSessionData = SessionData.builder().host(host).build();
    PublicSiteInfo expected = new PublicSiteInfo();
    PublicSiteInfoApiPort siteInfoApiPort =
        sessionData -> {
          assertEquals(expectedSessionData, sessionData);
          return expected;
        };
    PublicSiteInfoService service = new PublicSiteInfoService(siteInfoApiPort);

    PublicSiteInfo result = service.getPublicSiteInfo(host);

    assertSame(expected, result);
  }
}
