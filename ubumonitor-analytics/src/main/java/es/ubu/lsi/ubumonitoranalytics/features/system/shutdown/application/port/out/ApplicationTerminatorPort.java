package es.ubu.lsi.ubumonitoranalytics.features.system.shutdown.application.port.out;

public interface ApplicationTerminatorPort {
  void terminate();

  void terminateAfterResponse();
}
