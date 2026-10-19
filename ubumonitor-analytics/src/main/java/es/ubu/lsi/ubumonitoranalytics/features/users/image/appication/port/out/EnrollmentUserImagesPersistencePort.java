package es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.EnrollmentImagesDocumentCommand;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.model.EnrollmentUserImage;
import java.util.List;

public interface EnrollmentUserImagesPersistencePort {
  List<EnrollmentUserImage> getCourseEnrollmentImages(EnrollmentImagesDocumentCommand command);

  List<EnrollmentUserImage> getCourseEnrollmentImages(
      EnrollmentImagesDocumentCommand command, byte[] imageData, String contentType);
}
