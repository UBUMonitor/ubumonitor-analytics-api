package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper;

import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.mapstruct.Condition;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/** Provides shared conversions for Moodle content and persistence mappings. */
@Mapper(componentModel = "spring")
public class MapperUtils {

  private static final Parser MARKDOWN_PARSER = Parser.builder().build();
  private static final HtmlRenderer MARKDOWN_RENDERER = HtmlRenderer.builder().build();

  public OffsetDateTime toOffsetDateTime(Instant instant) {
    return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
  }

  public Instant toInstant(OffsetDateTime offsetDateTime) {
    return offsetDateTime == null ? null : offsetDateTime.toInstant();
  }

  public LocalDateTime toLocalDateTime(OffsetDateTime offsetDateTime) {
    return offsetDateTime == null ? null : offsetDateTime.toLocalDateTime();
  }

  public OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
    return localDateTime == null ? null : localDateTime.atOffset(ZoneOffset.UTC);
  }

  public LocalDateTime toLocalDateTime(String localDateTime) {
    if (localDateTime == null) {
      return null;
    }
    return LocalDateTime.parse(localDateTime);
  }

  public String toLocalDateTimeString(LocalDateTime localDateTime) {
    if (localDateTime == null) {
      return null;
    }
    return localDateTime.toString();
  }

  @Named("current")
  public LocalDateTime mapCurrent(Object ignored) {
    return LocalDateTime.now();
  }

  public static Boolean integerToBoolean(Integer value) {
    return value != null && value > 0;
  }

  @Named("profileUrl")
  public static URI toPrivateProfileUrl(URI profileUrl) {
    if (profileUrl == null) {
      return null;
    }
    UriComponents original = UriComponentsBuilder.fromUri(profileUrl).build();

    return UriComponentsBuilder.fromUri(profileUrl)
        .replacePath("/webservice" + original.getPath())
        .build()
        .toUri();
  }

  public static Integer booleanToInteger(Boolean bool) {
    if (bool == null) {
      return null;
    }
    return bool ? 1 : 0;
  }

  @Condition
  @Named("notNull")
  public static boolean notNull(Object object) {
    return object != null;
  }

  /**
   * Converts Moodle content to plain text according to its summary format.
   *
   * @param content content returned by the Moodle web service
   * @param format Moodle format identifier
   * @return plain text, or {@code null} when the content is {@code null}
   */
  @Named("parseMoodleContent")
  public static String parseMoodleContent(String content, Integer format) {
    if (content == null) {
      return null;
    }
    if (format == null) {
      return content;
    }
    return switch (format) {
      case 0 -> // MOODLE legacy
          stripMoodleTags(content);
      case 1 -> // HTML
          parseHtml(content);
      case 4 -> // Markdown
          parseMarkdown(content);
      default -> // Plain text.
          content;
    };
  }

  /** Converts HTML to plain text using Jsoup. */
  @Named("parseHtml")
  public static String parseHtml(String html) {
    if (html == null) {
      return null;
    }
    return Jsoup.parse(html).text();
  }

  /** Converts Markdown to plain text. */
  @Named("parseMarkdown")
  public static String parseMarkdown(String markdown) {
    if (markdown == null) {
      return null;
    }
    String html = MARKDOWN_RENDERER.render(MARKDOWN_PARSER.parse(markdown));
    return Jsoup.parse(html).text();
  }

  private static String stripMoodleTags(String moodleContent) {

    return moodleContent.replaceAll("\\[/?\\w+\\\\]", "");
  }

  @Named("htmlPercentage")
  public static BigDecimal htmlPercentage(String percentage) {
    if (percentage == null) {
      return null;
    }

    // Initial cleanup and normalization.
    String cleaned = MapperUtils.parseHtml(percentage).replace(",", ".").replaceAll("[^0-9.]", "");

    // Content validation.
    if (cleaned.isEmpty() || cleaned.equals(".")) {
      return null;
    }

    return new BigDecimal(cleaned).divide(new BigDecimal("100"), MathContext.DECIMAL128);
  }

  @Named("parseNumber")
  public static BigDecimal parseNumber(String input) {
    if (input == null || input.trim().isEmpty()) {
      return null;
    }

    try {
      input = input.trim();

      // Comma used as the decimal separator.
      if (input.contains(",") && !input.contains(".")) {
        input = input.replace(",", ".");
      }
      // Both separators are present (thousands and decimal).
      else if (input.contains(",") && input.contains(".")) {
        // The last separator is the decimal separator.
        if (input.lastIndexOf(",") > input.lastIndexOf(".")) {
          input = input.replace(".", "").replace(",", ".");
        } else {
          input = input.replace(",", "");
        }
      }

      return new BigDecimal(input);

    } catch (Exception _) {
      return null;
    }
  }

  @Named("parseInteger")
  public static Integer parseInteger(String value) {
    try {
      return Integer.parseInt(value);
    } catch (Exception _) {
      return null;
    }
  }

  public static Integer longToInteger(Long value) {
    if (value == null) {
      return null;
    }
    return value.intValue();
  }

  public static OffsetDateTime unixToOffsetDateTime(Integer unixTimestamp) {
    if (unixTimestamp == null || unixTimestamp <= 0) {
      return null;
    }
    Instant instant = Instant.ofEpochSecond(unixTimestamp);
    return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
  }

  public static Integer toUnixTimestamp(OffsetDateTime offsetDateTime) {
    if (offsetDateTime == null) {
      return null;
    }

    return (int) offsetDateTime.toEpochSecond();
  }

  public static URI toUri(String stringUri) {
    if (stringUri == null) {
      return null;
    }

    try {
      return new URI(stringUri);
    } catch (URISyntaxException _) {
      return null;
    }
  }

  public String map(URI uri) {
    return uri == null ? null : uri.toString();
  }
}
