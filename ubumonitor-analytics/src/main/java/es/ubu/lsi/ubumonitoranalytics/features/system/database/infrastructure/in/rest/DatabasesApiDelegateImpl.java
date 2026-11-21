package es.ubu.lsi.ubumonitoranalytics.features.system.database.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.DatabasesApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.DatabaseSnapshotInfoDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.DatabasesInfoResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.system.database.application.port.in.CreateDatabaseSnapshotUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.system.database.application.port.in.DatabaseInfoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatabasesApiDelegateImpl implements DatabasesApiDelegate {

  private final DatabaseInfoUseCase databaseInfoUseCase;
  private final CreateDatabaseSnapshotUseCase createDatabaseSnapshotUseCase;
  private final DatabasesApiDelegateImplMapper mapper;

  @Override
  public ResponseEntity<DatabasesInfoResponseDto> listDatabases() {
    DatabasesInfoResponseDto responseDto = new DatabasesInfoResponseDto();
    responseDto.databases(mapper.toDatabaseHostInfoDto(databaseInfoUseCase.getDatabasesInfo()));
    return ResponseEntity.ok(responseDto);
  }

  @Override
  public ResponseEntity<DatabaseSnapshotInfoDto> createDatabaseSnapshot() {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(mapper.toDatabaseSnapshotInfoDto(createDatabaseSnapshotUseCase.createSnapshot()));
  }
}
