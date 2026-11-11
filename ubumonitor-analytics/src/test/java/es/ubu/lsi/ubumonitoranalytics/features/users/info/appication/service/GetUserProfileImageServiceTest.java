package es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.out.UserProfileImagePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.image.UserImage;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.SessionStorePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import java.net.URI;
import org.junit.jupiter.api.Test;

class GetUserProfileImageServiceTest {

  @Test
  void runsAllImagePersistenceOperationsWithinTheSessionScope() {
    UserProfileImagePersistencePort imagePersistencePort =
        mock(UserProfileImagePersistencePort.class);
    SessionStorePort sessionStorePort = mock(SessionStorePort.class);
    CurrentSessionContext currentSessionContext = new CurrentSessionContext();
    GetUserProfileImageService service =
        new GetUserProfileImageService(
            imagePersistencePort, sessionStorePort, currentSessionContext);
    URI host = URI.create("https://school.example");
    SessionData sessionData = SessionData.builder().username("teacher").build();
    UserImage storedImage =
        UserImage.builder()
            .hexHash("image-hash")
            .contentType("image/png")
            .image(new byte[] {1})
            .build();
    when(sessionStorePort.getSession(host, "teacher")).thenReturn(sessionData);
    doAnswer(
            invocation -> {
              assertSame(sessionData, currentSessionContext.getSessionData());
              return "image-hash";
            })
        .when(imagePersistencePort)
        .getImageHash(13L);
    doAnswer(
            invocation -> {
              assertSame(sessionData, currentSessionContext.getSessionData());
              return storedImage;
            })
        .when(imagePersistencePort)
        .fetchUserImage(13L);

    UserImage result = service.getUserImage(13L, host, "teacher", null);

    assertSame(storedImage, result);
    assertTrue(result.isModified());
    assertNull(currentSessionContext.getSessionData());
  }
}
