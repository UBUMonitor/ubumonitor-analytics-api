package es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.service;


import es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.in.GetUserImageUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.out.ImagePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.model.UserImage;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.session.SessionStorePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import es.ubu.lsi.ubumonitoranalytics.util.ImageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserImageService implements GetUserImageUseCase {

    private final ImagePersistencePort imagePersistencePort;
    private final SessionStorePort sessionStorePort;
    private final CurrentSessionContext currentSessionContext;

    @Override
    public UserImage getUserImage(Integer userId, URI host, String username, String ifNoneMatch) {
        String normalized = normalize(ifNoneMatch);
        SessionData sessionData = sessionStorePort.getSession(host, username);

        if (sessionData == null) {
            return getPlaceHolder(normalized);
        }

        currentSessionContext.setSessionData(sessionData);
        String imageHash = imagePersistencePort.getImageHash(userId);

        // Usuario sin imagen -> placeholder
        if (imageHash == null) {
            return getPlaceHolder(normalized);
        }

        // Match ETag -> 304
        if (imageHash.equals(normalized)) {

            return UserImage.builder()
                .hexHash(imageHash)
                .image(null)
                .isModified(false)
                .build();
        }

        UserImage userImage = imagePersistencePort.fetchUserImage(userId);
        userImage.setModified(true);

        return userImage;
    }

    private static UserImage getPlaceHolder(String normalized) {
        String etag = ImageUtil.getPlaceholderEtag();
        boolean modified = !etag.equals(normalized);

        return UserImage.builder()
            .hexHash(etag)
            .contentType("image/png")
            .image(modified ? ImageUtil.getPlaceholder() : null)
            .isModified(modified)
            .build();
    }

    private String normalize(String ifNoneMatch) {

        if (ifNoneMatch == null) {
            return null;
        }

        return ifNoneMatch.replace("\"", "");
    }



}

