package es.ubu.lsi.moodle.api.gradereport;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.request.GetGradeItemsRequestApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradeitems.response.GetGradeItemsResponseApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.request.GetGradesTableRequestApi;
import es.ubu.lsi.moodle.model.gradereport.user.getgradestable.response.GetGradesTableResponseApi;
import lombok.RequiredArgsConstructor;

/** Implements Moodle user grade-report calls through the shared client. */
@RequiredArgsConstructor
public class GradeReportUserApiImpl implements GradeReportUserApi {
  private final Client client;

  @Override
  public GetGradesTableResponseApi getGradesTable(GetGradesTableRequestApi request) {
    return client.execute(request, GetGradesTableResponseApi.class);
  }

  @Override
  public GetGradeItemsResponseApi getGradeItems(GetGradeItemsRequestApi request) {
    return client.execute(request, GetGradeItemsResponseApi.class);
  }
}
