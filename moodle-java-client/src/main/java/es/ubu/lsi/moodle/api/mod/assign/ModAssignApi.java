package es.ubu.lsi.moodle.api.mod.assign;

import es.ubu.lsi.moodle.model.mod.assign.getassignments.request.GetAssignmentsRequestApi;
import es.ubu.lsi.moodle.model.mod.assign.getassignments.response.GetAssignmentsResponseApi;
import es.ubu.lsi.moodle.model.mod.assign.getsubmissions.request.GetSubmissionsRequestApi;
import es.ubu.lsi.moodle.model.mod.assign.getsubmissions.response.GetSubmissionsResponseApi;

/** Exposes Moodle assignment operations. */
public interface ModAssignApi {
  /**
   * @param request query for courses and assignments available to the user
   * @return matching courses and assignments
   */
  GetAssignmentsResponseApi getAssignments(GetAssignmentsRequestApi request);

  /**
   * @param request query for submissions in one or more assignments
   * @return matching assignment submissions
   */
  GetSubmissionsResponseApi getSubmissions(GetSubmissionsRequestApi request);
}
