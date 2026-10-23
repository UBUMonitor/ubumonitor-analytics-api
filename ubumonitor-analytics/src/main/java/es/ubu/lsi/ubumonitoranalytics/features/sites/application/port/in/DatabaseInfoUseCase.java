package es.ubu.lsi.ubumonitoranalytics.features.sites.application.port.in;

import es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model.DatabaseHostInfo;
import java.util.List;

public interface DatabaseInfoUseCase {
  List<DatabaseHostInfo> getDatabasesInfo();
}
