package es.ubu.lsi.moodle.api.mod.assign;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.mod.assign.getsubmissions.request.GetSubmissionsRequestApi;
import es.ubu.lsi.moodle.model.mod.assign.getsubmissions.response.GetSubmissionsResponseApi;
import lombok.RequiredArgsConstructor;

/** Implements Moodle assignment calls through the shared client. */
@RequiredArgsConstructor
public class ModAssignApiImpl implements ModAssignApi {
  private final Client client;

  @Override
  public GetSubmissionsResponseApi getSubmissions(GetSubmissionsRequestApi request) {
    return client.execute(request, GetSubmissionsResponseApi.class);
  }
}
