package es.ubu.lsi.ubumonitoranalytics.shared.application.image;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.util.HashUtil;
import java.io.IOException;
import java.io.UncheckedIOException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserPlaceholderImage {

  private static final byte[] PLACEHOLDER = loadPlaceholder();
  private static final String PLACEHOLDER_ETAG = HashUtil.imageHash(PLACEHOLDER);
  private static final String CONTENT_TYPE = MediaType.IMAGE_PNG_VALUE;

  private static byte[] loadPlaceholder() {
    try {
      return new ClassPathResource("images/user_placeholder.png").getContentAsByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException("No se pudo cargar images/user_placeholder.png", e);
    }
  }

  public static byte[] getPlaceholder() {
    return PLACEHOLDER;
  }

  public static String getPlaceholderEtag() {
    return PLACEHOLDER_ETAG;
  }

  public static String getContentType() {
    return CONTENT_TYPE;
  }
}
