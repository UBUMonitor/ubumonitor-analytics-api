package es.ubu.lsi.moodle.api.gradereport;

import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.request.GetGradeItemsRequestApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.GetGradeItemsResponseApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.request.GetGradesTableRequestApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.GetGradesTableResponseApi;

/** Exposes Moodle user grade-report operations. */
public interface GradeReportUserApi {

  /**
   * @param request grade table query
   * @return the requested grade table
   */
  GetGradesTableResponseApi getGradesTable(GetGradesTableRequestApi request);

  /**
   * @param request grade item query
   * @return grade items for the requested user and course
   */
  GetGradeItemsResponseApi getGradeItems(GetGradeItemsRequestApi request);
}
