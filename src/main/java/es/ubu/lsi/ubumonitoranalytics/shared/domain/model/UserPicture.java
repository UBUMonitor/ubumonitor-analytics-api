package es.ubu.lsi.ubumonitoranalytics.shared.domain.model;

import java.net.URI;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
