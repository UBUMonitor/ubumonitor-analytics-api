package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MapperUtilsTest {

  @Test
  void parseNumberReturnsNullForNullEmptyAndInvalidValues() {
    assertNull(MapperUtils.parseNumber(null));
    assertNull(MapperUtils.parseNumber(""));
    assertNull(MapperUtils.parseNumber("   "));
    assertNull(MapperUtils.parseNumber("not-a-number"));
  }

  @Test
  void parseNumberParsesDecimalLimits() {
    assertEquals(new BigDecimal("123.45"), MapperUtils.parseNumber("123.45"));
    assertEquals(new BigDecimal("-123.45"), MapperUtils.parseNumber("-123,45"));
    assertEquals(new BigDecimal("2147483648"), MapperUtils.parseNumber("2147483648"));
  }

  @Test
  void parseIntegerReturnsNullForNullEmptyInvalidAndOverflowValues() {
    assertNull(MapperUtils.parseInteger(null));
    assertNull(MapperUtils.parseInteger(""));
    assertNull(MapperUtils.parseInteger("not-an-integer"));
    assertNull(MapperUtils.parseInteger("2147483648"));
  }

  @Test
  void parseIntegerAcceptsIntegerLimits() {
    assertEquals(Integer.MIN_VALUE, MapperUtils.parseInteger("-2147483648"));
    assertEquals(Integer.MAX_VALUE, MapperUtils.parseInteger("2147483647"));
  }

  @Test
  void toUriReturnsNullForInvalidInput() {
    assertNull(MapperUtils.toUri("https://example.test/invalid path"));
  }
}
