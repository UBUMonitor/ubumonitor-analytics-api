package es.ubu.lsi.moodleanalytics.features.auth.application.port.in;

import es.ubu.lsi.moodleanalytics.features.auth.domain.model.JwtToken;
import es.ubu.lsi.moodleanalytics.features.auth.domain.model.auth.AuthInput;

public interface AuthUseCase {

    JwtToken loginByToken(AuthInput tokenAuthInput);
    JwtToken loginByCredentials(AuthInput authInput);
    JwtToken loginOffline(AuthInput offlineAuthInput);
}
