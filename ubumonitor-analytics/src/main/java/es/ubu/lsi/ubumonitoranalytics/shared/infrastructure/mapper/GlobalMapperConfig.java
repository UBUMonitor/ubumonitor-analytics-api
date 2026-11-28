package es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.NullValuePropertyMappingStrategy;

/** Defines shared MapStruct settings for application mappers. */
@MapperConfig(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = MapperUtils.class,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface GlobalMapperConfig {}
