package es.ubu.lsi.moodle.api.core.course;

import es.ubu.lsi.moodle.model.core.course.getcontents.request.GetCourseContentsRequestApi;
import es.ubu.lsi.moodle.model.core.course.getcontents.response.GetCourseContentsResponseApi;
import java.util.List;

/** Exposes Moodle core course operations. */
public interface CoreCourseApi {

  /**
   * @param request request specifying which course content to retrieve
   * @return course content returned by Moodle
   */
  List<GetCourseContentsResponseApi> getContents(GetCourseContentsRequestApi request);
}
