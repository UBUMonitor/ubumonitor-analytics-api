package es.ubu.lsi.ubumonitoranalytics.features.course.assignments.infrastructure.out.moodle;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IdsRequest<T> {
  private final List<T> ids;
}
