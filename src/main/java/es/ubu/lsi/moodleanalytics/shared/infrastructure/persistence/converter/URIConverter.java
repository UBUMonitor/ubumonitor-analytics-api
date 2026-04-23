package es.ubu.lsi.moodleanalytics.shared.infrastructure.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.net.URI;

@Converter(autoApply = true)
public class URIConverter implements AttributeConverter<URI, String> {
    @Override
    public String convertToDatabaseColumn(URI attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public URI convertToEntityAttribute(String dbData) {
        return dbData == null ? null : URI.create(dbData);
    }
}
