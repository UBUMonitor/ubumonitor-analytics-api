package es.ubu.lsi.ubumonitoranalytics.features.course.assignments.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.domain.model.AssignmentSubmission;
import es.ubu.lsi.ubumonitoranalytics.features.course.assignments.domain.model.CourseAssignment;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.AssignmentSubmissionsRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.CourseAssignmentsRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.util.List;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface CourseAssignmentSubmissionsPersistenceMapper {

  @Mapping(target = "createdAt", expression = "java(now)")
  @Mapping(target = "updatedAt", expression = "java(now)")
  @Mapping(target = "active", constant = "true")
  CourseAssignmentsRecord toAssignmentRecord(CourseAssignment assignment, @Context Long now);

  List<CourseAssignmentsRecord> toAssignmentRecords(
      List<CourseAssignment> assignments, @Context Long now);

  CourseAssignment toAssignmentDomain(CourseAssignmentsRecord record);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "active", constant = "true")
  @Mapping(target = "updatedAt", expression = "java(now)")
  AssignmentSubmissionsRecord toRecord(AssignmentSubmission submission, @Context Long now);

  List<AssignmentSubmissionsRecord> toRecords(
      List<AssignmentSubmission> submissions, @Context Long now);

  AssignmentSubmission toDomain(AssignmentSubmissionsRecord record);
}
