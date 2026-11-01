package es.ubu.lsi.ubumonitoranalytics.features.course.grades.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.GradeItem;
import es.ubu.lsi.ubumonitoranalytics.features.course.grades.domain.model.UserGrade;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.GradesRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersGradesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.time.OffsetDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface GradesSavePersistenceMapper {

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "courseId", source = "courseId")
  @Mapping(target = "updatedAt", source = "now")
  @Mapping(target = "active", constant = "true")
  @Mapping(target = "id", source = "gradeItem.id")
  @Mapping(target = "gradeMin", source = "gradeItem.gradeMin")
  @Mapping(target = "gradeMax", source = "gradeItem.gradeMax")
  @Mapping(target = "parentId", source = "gradeItem.parentId")
  @Mapping(target = "itemType", source = "gradeItem.itemType")
  @Mapping(target = "name", source = "gradeItem.name")
  GradesRecord toRecord(GradeItem gradeItem, Long courseId, OffsetDateTime now);

  @Mapping(target = "percentage", source = "userGrade.gradePercentageRaw")
  @Mapping(target = "gradeId", source = "userGrade.gradeItemId")
  @Mapping(target = "grade", source = "userGrade.gradeRaw")
  @Mapping(target = "userId", source = "userGrade.userId")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", source = "now")
  @Mapping(target = "active", constant = "true")
  UsersGradesRecord toRecord(UserGrade userGrade, OffsetDateTime now);
}
