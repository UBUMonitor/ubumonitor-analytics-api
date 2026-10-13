package es.ubu.lsi.moodle.api.core.completion;

import es.ubu.lsi.moodle.model.core.completion.getactivitiescompletionstatus.request.GetActivitiesCompletionStatusRequestApi;
import es.ubu.lsi.moodle.model.core.completion.getactivitiescompletionstatus.response.GetActivitiesCompletionStatusResponseApi;

/** Exposes Moodle activity completion operations. */
public interface CoreCompletionApi {

  /**
   * @param request activity completion query
   * @return completion status for the requested activities
   */
  GetActivitiesCompletionStatusResponseApi getActivitiesCompletionStatus(
      GetActivitiesCompletionStatusRequestApi request);
}
