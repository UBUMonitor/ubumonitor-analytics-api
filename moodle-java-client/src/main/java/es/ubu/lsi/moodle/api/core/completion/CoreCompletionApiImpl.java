package es.ubu.lsi.moodle.api.core.completion;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.core.completion.getactivitiescompletionstatus.request.GetActivitiesCompletionStatusRequestApi;
import es.ubu.lsi.moodle.model.core.completion.getactivitiescompletionstatus.response.GetActivitiesCompletionStatusResponseApi;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CoreCompletionApiImpl implements CoreCompletionApi {
  private final Client client;

  @Override
  public GetActivitiesCompletionStatusResponseApi getActivitiesCompletionStatus(
      GetActivitiesCompletionStatusRequestApi request) {
    return client.execute(request, GetActivitiesCompletionStatusResponseApi.class);
  }
}
