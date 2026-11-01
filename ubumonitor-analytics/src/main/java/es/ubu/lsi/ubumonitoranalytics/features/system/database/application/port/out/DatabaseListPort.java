package es.ubu.lsi.ubumonitoranalytics.features.system.database.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.system.database.domain.model.DatabaseHostInfo;
import java.util.List;

public interface DatabaseListPort {
  List<DatabaseHostInfo> getDatabaseList();
}
