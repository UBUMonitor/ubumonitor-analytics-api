package es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.EnrollmentImagesDocumentCommand;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.model.EnrollmentUserImage;
import java.util.List;

public interface EnrollmentUserImagesDocumentPort {

  byte[] generateEnrollmentImagesDocument(
      List<EnrollmentUserImage> users, EnrollmentImagesDocumentCommand command);
}
