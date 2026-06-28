package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.NullValuePropertyMappingStrategy;

@MapperConfig(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = MapperUtils.class)
public interface GlobalMapperConfig {}
