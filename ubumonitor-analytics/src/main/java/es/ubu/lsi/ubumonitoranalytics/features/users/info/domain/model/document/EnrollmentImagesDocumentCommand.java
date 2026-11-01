package es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.document;

import java.net.URI;
import java.util.List;
import lombok.Data;

@Data
public class EnrollmentImagesDocumentCommand {
  private List<Long> userIds;
  private String headerText;
  private URI defaultImageUri;
  private boolean useDefaultPhoto;
}
