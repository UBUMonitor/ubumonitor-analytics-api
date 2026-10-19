package es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.UserGrade;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.GradesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersGradesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface GradesFetchPersistenceMapper {

  @Mapping(target = "itemInstance", ignore = true)
  GradeItem toGradeItem(GradesRecord record);

  @Mapping(source = "gradeId", target = "gradeItemId")
  @Mapping(source = "grade", target = "gradeRaw")
  @Mapping(source = "percentage", target = "gradePercentageRaw")
  UserGrade toUserGrade(UsersGradesRecord record);
}
