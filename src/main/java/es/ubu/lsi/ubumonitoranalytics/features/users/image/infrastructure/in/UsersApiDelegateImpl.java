package es.ubu.lsi.ubumonitoranalytics.features.users.image.infrastructure.in;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.UsersApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.in.GetUserImageUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.model.UserImage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class UsersApiDelegateImpl implements UsersApiDelegate {

    private final GetUserImageUseCase getUserImageUseCase;

    @Override
    public ResponseEntity<Resource> getUserImagesProfile(Integer userId, String ifNoneMatch) {

        UserImage userImage = getUserImageUseCase.getUserImage(userId, ifNoneMatch);

        if (!userImage.isModified()) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                .eTag(userImage.getHexHash())
                .build();
        }

        return ResponseEntity.ok()
            .eTag(userImage.getHexHash())
            .contentType(MediaType.parseMediaType(userImage.getContentType()))
            .body(new ByteArrayResource(userImage.getImage()));
    }
}

