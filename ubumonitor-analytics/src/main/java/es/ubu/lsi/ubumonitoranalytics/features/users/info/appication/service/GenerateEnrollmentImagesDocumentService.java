package es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.service;

import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.in.GenerateEnrollmentImagesDocumentUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.out.EnrollmentUserImagesDocumentPort;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.out.EnrollmentUserImagesPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.document.EnrollmentImagesDocumentCommand;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.image.EnrollmentUserImage;
import es.ubu.lsi.ubumonitoranalytics.util.ImageUtil;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GenerateEnrollmentImagesDocumentService
    implements GenerateEnrollmentImagesDocumentUseCase {
  private final EnrollmentUserImagesPersistencePort enrollmentUserImagesPersistencePort;
  private final EnrollmentUserImagesDocumentPort enrollmentUserImagesDocumentPort;

  @Override
  @Transactional(readOnly = true)
  public byte[] generateEnrollmentImagesDocument(EnrollmentImagesDocumentCommand command) {
    List<EnrollmentUserImage> users = findUsers(command);
    return enrollmentUserImagesDocumentPort.generateEnrollmentImagesDocument(users, command);
  }

  private List<EnrollmentUserImage> findUsers(EnrollmentImagesDocumentCommand command) {
    if (!command.isUseDefaultPhoto()) {
      return enrollmentUserImagesPersistencePort.getCourseEnrollmentImages(command);
    }

    if (command.getDefaultImageUri() == null) {
      return enrollmentUserImagesPersistencePort.getCourseEnrollmentImages(
          command, ImageUtil.getPlaceholder(), ImageUtil.getContentType());
    }

    return findUsersWithCustomDefaultImage(command);
  }

  @SneakyThrows
  private List<EnrollmentUserImage> findUsersWithCustomDefaultImage(
      EnrollmentImagesDocumentCommand command) {

    URI defaultImage = command.getDefaultImageUri();
    return enrollmentUserImagesPersistencePort.getCourseEnrollmentImages(command, null, null);
  }
}
