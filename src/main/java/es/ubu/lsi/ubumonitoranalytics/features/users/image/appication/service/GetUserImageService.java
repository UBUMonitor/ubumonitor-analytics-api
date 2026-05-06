package es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.service;


import es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.in.GetUserImageUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.out.ImagePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.model.UserImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetUserImageService implements GetUserImageUseCase {

    private final ImagePersistencePort imagePersistencePort;

    @Override
    @Transactional(readOnly = true)
    public Optional<UserImage> getUserImage(Integer userId, String ifNoneMatch) {

        String imageHash = imagePersistencePort.getImageHash(userId);
        if (imageHash == null) {
            return Optional.empty();
        }

        String normalized = normalize(ifNoneMatch);

        if (imageHash.equals(normalized)) {
            return Optional.of(UserImage.builder()
                .hexHash(imageHash)
                .image(null)
                .isModified(false)
                .build());
        }

        byte[] imageData = imagePersistencePort.fetchUserImage(userId);

        return Optional.of(UserImage.builder()
            .hexHash(imageHash)
            .image(imageData)
            .isModified(true)
            .build());
    }


    private String normalize(String ifNoneMatch) {
        if (ifNoneMatch == null) {
            return null;
        }
        return ifNoneMatch.replace("\"", "");
    }
}
