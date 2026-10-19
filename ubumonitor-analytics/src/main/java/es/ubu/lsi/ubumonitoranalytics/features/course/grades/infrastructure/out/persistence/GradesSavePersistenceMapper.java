package es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.UserGrade;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.GradesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersGradesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = GlobalMapperConfig.class)
public interface GradesSavePersistenceMapper {

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
  @Mapping(target = "courseId", ignore = true)
  @Mapping(target = "active", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void toRecord(GradeItem gradeItem, @MappingTarget GradesRecord record);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
  @Mapping(target = "gradeId", source = "gradeItemId")
  @Mapping(target = "grade", source = "gradeRaw")
  @Mapping(target = "percentage", source = "gradePercentageRaw")
  @Mapping(target = "active", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void toRecord(UserGrade userGrade, @MappingTarget UsersGradesRecord record);
}
