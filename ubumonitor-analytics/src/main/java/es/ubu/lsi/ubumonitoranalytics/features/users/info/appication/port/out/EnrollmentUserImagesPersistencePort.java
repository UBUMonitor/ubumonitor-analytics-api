package es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.document.EnrollmentImagesDocumentCommand;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.image.EnrollmentUserImage;
import java.util.List;

public interface EnrollmentUserImagesPersistencePort {
  List<EnrollmentUserImage> getCourseEnrollmentImages(EnrollmentImagesDocumentCommand command);

  List<EnrollmentUserImage> getCourseEnrollmentImages(
      EnrollmentImagesDocumentCommand command, byte[] imageData, String contentType);
}
