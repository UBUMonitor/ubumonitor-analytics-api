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
    default OffsetDateTime map(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }

    default Instant map(OffsetDateTime offsetDateTime) {
        return offsetDateTime == null ? null : offsetDateTime.toInstant();
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
