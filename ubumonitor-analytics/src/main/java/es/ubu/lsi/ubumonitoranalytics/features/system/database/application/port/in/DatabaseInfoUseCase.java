package es.ubu.lsi.ubumonitoranalytics.features.system.database.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.system.database.domain.model.DatabaseHostInfo;
import java.util.List;

public interface DatabaseInfoUseCase {
  List<DatabaseHostInfo> getDatabasesInfo();
}
