package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import java.net.URI;
import org.junit.jupiter.api.Test;

class ConcurrentHashMapSessionStoreAdapterTest {

  @Test
  void provisionalSessionWithNullUsernameDoesNotMatchAUserSessionLookup() {
    ConcurrentHashMapSessionStoreAdapter sessionStore = new ConcurrentHashMapSessionStoreAdapter();
    URI host = URI.create("https://moodle.test");
    sessionStore.saveSession(
        "provisional-token", SessionData.builder().token("provisional-token").host(host).build());

    assertFalse(sessionStore.hasSession(host, "teacher"));
    assertFalse(sessionStore.hasSession(host, null));
    assertNull(sessionStore.getSession(host, "teacher"));
    assertNull(sessionStore.getSession(host, null));
  }
}
