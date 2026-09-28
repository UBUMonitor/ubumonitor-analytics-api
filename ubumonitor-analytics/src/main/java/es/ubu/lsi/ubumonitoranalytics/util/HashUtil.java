package es.ubu.lsi.ubumonitoranalytics.util;

import java.security.MessageDigest;
import java.util.HexFormat;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HashUtil {

  @SneakyThrows
  public static String imageHash(byte[] image) {
    MessageDigest md = MessageDigest.getInstance("SHA-256");

    return HexFormat.of().formatHex(md.digest(image));
  }
}
