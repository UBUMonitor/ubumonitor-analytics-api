package es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.document.EnrollmentImagesDocumentCommand;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.image.EnrollmentUserImage;
import java.util.List;

public interface EnrollmentUserImagesDocumentPort {

  byte[] generateEnrollmentImagesDocument(
      List<EnrollmentUserImage> users, EnrollmentImagesDocumentCommand command);
}
