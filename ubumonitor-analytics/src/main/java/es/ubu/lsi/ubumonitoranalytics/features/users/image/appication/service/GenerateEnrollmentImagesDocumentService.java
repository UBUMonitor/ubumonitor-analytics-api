package es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.service;

import es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.in.GenerateEnrollmentImagesDocumentUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.out.EnrollmentUserImagesDocumentPort;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.out.EnrollmentUserImagesPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.EnrollmentImagesDocumentCommand;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.model.EnrollmentUserImage;
import es.ubu.lsi.ubumonitoranalytics.util.ImageUtil;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

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

    if (command.getDefaultImagePath() == null) {
      return enrollmentUserImagesPersistencePort.getCourseEnrollmentImages(
          command, ImageUtil.getPlaceholder(), ImageUtil.getContentType());
    }

    return findUsersWithCustomDefaultImage(command);
  }

  @SneakyThrows
  private List<EnrollmentUserImage> findUsersWithCustomDefaultImage(
      EnrollmentImagesDocumentCommand command) {

    File defaultImage = command.getDefaultImagePath();
    return enrollmentUserImagesPersistencePort.getCourseEnrollmentImages(
        command,
        FileCopyUtils.copyToByteArray(defaultImage),
        Files.probeContentType(defaultImage.toPath()));
  }
}
