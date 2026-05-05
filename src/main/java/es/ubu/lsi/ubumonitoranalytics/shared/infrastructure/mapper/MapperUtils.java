package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper;

import org.mapstruct.Mapper;

import java.time.Instant;
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
}
