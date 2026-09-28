package es.ubu.lsi.moodle.api.core.course;

import es.ubu.lsi.moodle.model.core.course.getcontents.request.GetCourseContentsRequestApi;
import es.ubu.lsi.moodle.model.core.course.getcontents.response.GetCourseContentsResponseApi;
import java.util.List;

public interface CoreCourseApi {

  List<GetCourseContentsResponseApi> getContents(GetCourseContentsRequestApi request);
}
