package es.ubu.lsi.ubumonitoranalytics.features.system.database.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.system.database.application.port.in.DatabaseInfoUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.system.database.application.port.out.DatabaseListPort;
import es.ubu.lsi.ubumonitoranalytics.features.system.database.domain.model.DatabaseHostInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatabasesInfoService implements DatabaseInfoUseCase {
  private final DatabaseListPort databaseListPort;

  @Override
  public List<DatabaseHostInfo> getDatabasesInfo() {
    return databaseListPort.getDatabaseList();
  }
}
