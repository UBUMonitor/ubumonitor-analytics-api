package es.ubu.lsi.ubumonitoranalytics.features.users.image.domain;

import java.io.File;
import java.util.List;
import java.util.Locale;
import lombok.Data;

@Data
public class EnrollmentImagesDocumentCommand {
  private Locale locale;
  private List<Integer> userIds;
  private String headerText;
  private File defaultImagePath;
  private boolean useDefaultPhoto;
}
