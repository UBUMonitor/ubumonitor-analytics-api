
package es.ubu.lsi.moodleanalytics.features.auth.infrastructure.out.moodle;



import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterLoginTokenRequestDto;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterLoginTokenResponseDto;
import es.ubu.lsi.moodleanalytics.features.auth.domain.model.auth.AuthInput;
import es.ubu.lsi.moodleanalytics.features.auth.domain.model.auth.LoginAuthResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MoodleAuthMapper {


    @Mapping(target = "username", source = "userName")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "host", source = "host")
    MoodleAdapterLoginTokenRequestDto toDto(AuthInput authInput);

    @Mapping(target = "moodleToken", source = "token")
    LoginAuthResult toDomain(MoodleAdapterLoginTokenResponseDto moodleAdapterLoginTokenResponseDto);
}
