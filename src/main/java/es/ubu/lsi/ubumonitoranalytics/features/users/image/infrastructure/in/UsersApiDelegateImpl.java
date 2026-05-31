package es.ubu.lsi.ubumonitoranalytics.features.users.image.infrastructure.in;

import es.ubu.lsi.ubumonitoranalytics.api.generated.api.UsersApiDelegate;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.in.GetUserImageUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.model.UserImage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.concurrent.TimeUnit;


@Component
@RequiredArgsConstructor
public class UsersApiDelegateImpl implements UsersApiDelegate {

    private final GetUserImageUseCase getUserImageUseCase;


    @Override
    public ResponseEntity<Resource> getUserImagesProfile(Integer userId, URI host, String username, String ifNoneMatch) {
        UserImage userImage = getUserImageUseCase.getUserImage(userId, host, username, ifNoneMatch);

        if (!userImage.isModified()) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                .eTag(userImage.getHexHash())
                .build();
        }

        return ResponseEntity.ok()
            .eTag(userImage.getHexHash())
            .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
            .contentType(MediaType.parseMediaType(userImage.getContentType()))
            .contentLength(userImage.getImage().length)
            .body(new ByteArrayResource(userImage.getImage()));
    }



}

