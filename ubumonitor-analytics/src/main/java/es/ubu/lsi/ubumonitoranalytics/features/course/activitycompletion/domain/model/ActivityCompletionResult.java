package es.ubu.lsi.ubumonitoranalytics.features.course.activitycompletion.domain.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActivityCompletionResult {
  private Long courseId;
  private List<ActivityCompletion> activityCompletions;
}
