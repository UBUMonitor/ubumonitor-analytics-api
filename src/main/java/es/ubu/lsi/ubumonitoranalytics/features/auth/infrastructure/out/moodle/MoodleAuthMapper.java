
package es.ubu.lsi.ubumonitoranalytics.features.auth.infrastructure.out.moodle;


import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterLoginTokenRequestDto;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterLoginTokenResponseDto;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.AuthInput;
import es.ubu.lsi.ubumonitoranalytics.features.auth.domain.model.auth.LoginAuthResult;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface MoodleAuthMapper {


    @Mapping(target = "username", source = "username")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "host", source = "host")
    MoodleAdapterLoginTokenRequestDto toDto(AuthInput authInput);

    @Mapping(target = "moodleToken", source = "token")
    LoginAuthResult toDomain(MoodleAdapterLoginTokenResponseDto moodleAdapterLoginTokenResponseDto);
}

