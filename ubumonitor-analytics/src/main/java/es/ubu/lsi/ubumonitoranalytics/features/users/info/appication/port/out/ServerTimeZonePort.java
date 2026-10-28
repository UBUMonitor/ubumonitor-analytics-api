package es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.out;

import java.time.ZoneId;

public interface ServerTimeZonePort {
  ZoneId getServerTimezone();
}
