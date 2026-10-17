package es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.EnrollmentImagesDocumentCommand;

public interface GenerateEnrollmentImagesDocumentUseCase {
  byte[] generateEnrollmentImagesDocument(EnrollmentImagesDocumentCommand command);
}
