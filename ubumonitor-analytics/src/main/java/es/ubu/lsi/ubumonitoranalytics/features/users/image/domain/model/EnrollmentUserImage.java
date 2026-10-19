package es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.model;

import lombok.Data;

@Data
public class EnrollmentUserImage {
  private Integer id;
  private String fullName;
  private UserImage userImage;
}
