package es.ubu.lsi.ubumonitoranalytics.shared.domain.model;

import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Holds a user's profile image data used during enrollment synchronization. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPicture {
  private URI url;
  private byte[] data;
  private String hexHash;
  private String contentType;
}
