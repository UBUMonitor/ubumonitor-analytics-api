package es.ubu.lsi.moodle.api.mod.assign;

import es.ubu.lsi.moodle.model.mod.assign.getsubmissions.request.GetSubmissionsRequestApi;
import es.ubu.lsi.moodle.model.mod.assign.getsubmissions.response.GetSubmissionsResponseApi;

/** Exposes Moodle assignment operations. */
public interface ModAssignApi {
  /**
   * @param request query for submissions in one or more assignments
   * @return matching assignment submissions
   */
  GetSubmissionsResponseApi getSubmissions(GetSubmissionsRequestApi request);
}
