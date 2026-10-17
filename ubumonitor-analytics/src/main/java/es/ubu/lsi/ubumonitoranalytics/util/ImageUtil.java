package es.ubu.lsi.ubumonitoranalytics.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ImageUtil {

  private static final byte[] PLACEHOLDER = loadPlaceholder();
  private static final String PLACEHOLDER_ETAG = HashUtil.imageHash(PLACEHOLDER);
  private static final String CONTENT_TYPE = "image/png";

  @SneakyThrows
  private static byte[] loadPlaceholder() {

    return new ClassPathResource("images/user_placeholder.png").getInputStream().readAllBytes();
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
