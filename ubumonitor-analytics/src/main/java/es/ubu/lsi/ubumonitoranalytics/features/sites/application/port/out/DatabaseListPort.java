package es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model.DatabaseHostInfo;
import java.util.List;

public interface DatabaseListPort {
  List<DatabaseHostInfo> getDatabaseList();
}
