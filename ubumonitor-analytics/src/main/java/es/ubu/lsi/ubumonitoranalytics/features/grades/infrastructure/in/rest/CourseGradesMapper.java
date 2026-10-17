package es.ubu.lsi.ubumonitoranalytics.features.grades.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;

/** Maps course grade information between REST DTOs and application models. */
@Mapper(config = GlobalMapperConfig.class)
public interface CourseGradesMapper {}
