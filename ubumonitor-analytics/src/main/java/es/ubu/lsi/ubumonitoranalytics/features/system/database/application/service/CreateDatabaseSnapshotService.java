package es.ubu.lsi.ubumonitoranalytics.features.system.database.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.system.database.application.port.in.CreateDatabaseSnapshotUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.system.database.application.port.out.DatabaseSnapshotPort;
import es.ubu.lsi.ubumonitoranalytics.features.system.database.domain.model.DatabaseSnapshot;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.UnauthorizedException;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.CurrentSessionPort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateDatabaseSnapshotService implements CreateDatabaseSnapshotUseCase {

  private final CurrentSessionPort currentSessionPort;
  private final DatabaseSnapshotPort databaseSnapshotPort;

  @Override
  public DatabaseSnapshot createSnapshot(String name) {
    SessionData session = currentSessionPort.getSessionData();
    if (session == null) {
      throw new UnauthorizedException("An authenticated session is required");
    }
    String normalizedName = name == null ? null : name.trim();
    if (normalizedName != null && normalizedName.isEmpty()) {
      normalizedName = null;
    }
    return databaseSnapshotPort.createSnapshot(session, normalizedName);
  }
}
