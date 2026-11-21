package es.ubu.lsi.ubumonitoranalytics.features.system.database.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.system.database.application.port.in.CreateDatabaseSnapshotUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.system.database.application.port.out.DatabaseSnapshotPort;
import es.ubu.lsi.ubumonitoranalytics.features.system.database.domain.model.DatabaseSnapshot;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.BadRequestException;
import es.ubu.lsi.ubumonitoranalytics.shared.application.exception.UnauthorizedException;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.CurrentSessionPort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateDatabaseSnapshotService implements CreateDatabaseSnapshotUseCase {

  private static final Pattern SNAPSHOT_NAME_PATTERN =
      Pattern.compile("^[A-Za-z0-9](?:[A-Za-z0-9 _.-]*[A-Za-z0-9])?$");
  private static final int MAX_NAME_LENGTH = 64;

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
    // The controller validates the untrimmed value; this also supports callers using the service
    // directly.
    if (normalizedName != null
        && (normalizedName.length() > MAX_NAME_LENGTH
            || !SNAPSHOT_NAME_PATTERN.matcher(normalizedName).matches())) {
      throw new BadRequestException("Snapshot name contains unsupported characters or length");
    }
    return databaseSnapshotPort.createSnapshot(session, normalizedName);
  }
}
