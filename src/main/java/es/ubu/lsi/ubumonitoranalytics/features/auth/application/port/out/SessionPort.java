package es.ubu.lsi.ubumonitoranalytics.features.auth.application.port.out;

import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.JwtToken;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.AuthInput;


public interface SessionPort {
    JwtToken generateSession(AuthInput authInput);
    boolean existDataBase(AuthInput authInput);
}

