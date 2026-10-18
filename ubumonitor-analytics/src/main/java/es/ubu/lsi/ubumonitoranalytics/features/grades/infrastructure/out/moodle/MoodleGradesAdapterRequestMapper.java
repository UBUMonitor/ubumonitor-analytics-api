package es.ubu.lsi.ubumonitoranalytics.features.grades.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.request.GetGradeItemsRequestApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.request.GetGradesTableRequestApi;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface MoodleGradesAdapterRequestMapper {

  @Mapping(target = "wsfunction", ignore = true)
  @Mapping(target = "args.courseid", source = "courseId")
  GetGradeItemsRequestApi toRequest(Integer courseId);

  @Mapping(target = "wsfunction", ignore = true)
  @Mapping(target = "args.courseid", source = "courseId")
  @Mapping(target = "args.userid", source = "userId")
  GetGradesTableRequestApi toGradesTableRequest(Integer courseId, Integer userId);
}
