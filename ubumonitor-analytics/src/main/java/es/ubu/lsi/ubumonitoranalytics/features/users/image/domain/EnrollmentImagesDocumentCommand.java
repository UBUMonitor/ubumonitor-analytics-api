package es.ubu.lsi.ubumonitoranalytics.features.users.image.domain;

import java.io.File;
import java.util.List;
import lombok.Data;

@Data
public class EnrollmentImagesDocumentCommand {
  private List<Long> userIds;
  private String headerText;
  private File defaultImagePath;
  private boolean useDefaultPhoto;
}
