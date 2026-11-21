package es.ubu.lsi.ubumonitoranalytics.features.system.database.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.system.database.domain.model.DatabaseSnapshot;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;

public interface DatabaseSnapshotPort {
  DatabaseSnapshot createSnapshot(SessionData session, String name);
}
