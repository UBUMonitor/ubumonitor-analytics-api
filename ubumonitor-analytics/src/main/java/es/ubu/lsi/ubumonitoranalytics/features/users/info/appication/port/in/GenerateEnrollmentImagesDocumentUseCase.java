package es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.document.EnrollmentImagesDocumentCommand;

public interface GenerateEnrollmentImagesDocumentUseCase {
  byte[] generateEnrollmentImagesDocument(EnrollmentImagesDocumentCommand command);
}
