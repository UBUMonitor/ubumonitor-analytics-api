package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;


@Mapper(componentModel = "spring")
public interface MapperUtils {
    default OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }

    default Instant toInstant(OffsetDateTime offsetDateTime) {
        return offsetDateTime == null ? null : offsetDateTime.toInstant();
    }

    default LocalDateTime toLocalDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime == null ? null : offsetDateTime.toLocalDateTime();
    }

    default OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.atOffset(ZoneOffset.UTC);
    }

    default LocalDateTime toLocalDateTime(String localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return LocalDateTime.parse(localDateTime);
    }
    default String toLocalDateTimeString(LocalDateTime localDateTime) {
        if  (localDateTime == null) {
            return null;
        }
        return localDateTime.toString();
    }

    default URI map(String uri) {
        if (uri == null) {
            return null;
        }
        return URI.create(uri);
    }

    default String map(URI uri) {
        return uri == null ? null : uri.toString();
    }

    @Named("current")
    default LocalDateTime mapCurrent(Object ignored) {
        return LocalDateTime.now();
    }

}
