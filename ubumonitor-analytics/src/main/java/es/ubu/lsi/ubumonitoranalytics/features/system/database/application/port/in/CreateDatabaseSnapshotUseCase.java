package es.ubu.lsi.ubumonitoranalytics.features.system.database.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.system.database.domain.model.DatabaseSnapshot;

public interface CreateDatabaseSnapshotUseCase {
  DatabaseSnapshot createSnapshot(String name);
}
