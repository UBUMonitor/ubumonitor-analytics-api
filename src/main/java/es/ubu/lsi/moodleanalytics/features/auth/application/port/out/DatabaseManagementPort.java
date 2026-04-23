package es.ubu.lsi.moodleanalytics.features.auth.application.port.out;

import java.net.URI;

public interface DatabaseManagementPort {

    void createAndInitializeDatabase(URI host, String userName, String dbPassword);
    boolean exists(URI host, String userName);
}
