package es.ubu.lsi.moodleanalytics.features.auth.application.port.out;

import es.ubu.lsi.moodleanalytics.features.auth.domain.model.auth.AuthInput;
import es.ubu.lsi.moodleanalytics.features.auth.domain.model.auth.LoginAuthResult;

public interface MoodleApiPort {

    LoginAuthResult login(AuthInput authInput);

}
