package es.ubu.lsi.ubumonitoranalytics.features.health.infrastructure.out;

import es.ubu.lsi.ubumonitoranalytics.features.health.application.port.out.ReadinessStatusPort;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringBootReadinessAdapter implements ReadinessStatusPort {

  private final ApplicationAvailability availability;

  @Override
  public boolean isAcceptingTraffic() {
    return availability.getReadinessState() == ReadinessState.ACCEPTING_TRAFFIC;
  }
}
