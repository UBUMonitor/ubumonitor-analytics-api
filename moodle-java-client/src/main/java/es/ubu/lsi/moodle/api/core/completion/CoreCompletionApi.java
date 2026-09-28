package es.ubu.lsi.moodle.api.core.completion;

import es.ubu.lsi.moodle.model.core.completion.getactivitiescompletionstatus.request.GetActivitiesCompletionStatusRequestApi;
import es.ubu.lsi.moodle.model.core.completion.getactivitiescompletionstatus.response.GetActivitiesCompletionStatusResponseApi;

public interface CoreCompletionApi {

  GetActivitiesCompletionStatusResponseApi getActivitiesCompletionStatus(
      GetActivitiesCompletionStatusRequestApi request);
}
