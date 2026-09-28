package es.ubu.lsi.moodle.api.gradereport;

import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.request.GetGradeItemsRequestApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.GetGradeItemsResponseApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.request.GetGradesTableRequestApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.GetGradesTableResponseApi;

public interface GradeReportUserApi {

  GetGradesTableResponseApi getGradesTable(GetGradesTableRequestApi request);

  GetGradeItemsResponseApi getGradeItems(GetGradeItemsRequestApi request);
}
