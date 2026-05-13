package es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.service;


import es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.in.GetUserImageUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.out.ImagePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.model.UserImage;
import es.ubu.lsi.ubumonitoranalytics.util.ImageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserImageService implements GetUserImageUseCase {

    private final ImagePersistencePort imagePersistencePort;


    @Override
    public UserImage getUserImage(Integer userId, String ifNoneMatch) {

        String normalized = normalize(ifNoneMatch);

        String imageHash = imagePersistencePort.getImageHash(userId);

        // Usuario sin imagen -> placeholder
        if (imageHash == null) {
            String etag = ImageUtil.getPlaceholderEtag();
            boolean modified = !etag.equals(normalized);

            return UserImage.builder()
                .hexHash(etag)
                .image(modified ? ImageUtil.getPlaceholder() : null)
                .isModified(modified)
                .build();
        }

        // Match ETag -> 304
        if (imageHash.equals(normalized)) {

            return UserImage.builder()
                .hexHash(imageHash)
                .image(null)
                .isModified(false)
                .build();
        }

        byte[] imageData = imagePersistencePort.fetchUserImage(userId);

        return UserImage.builder()
            .hexHash(imageHash)
            .image(imageData)
            .isModified(true)
            .build();
    }

    private String normalize(String ifNoneMatch) {

        if (ifNoneMatch == null) {
            return null;
        }

        return ifNoneMatch.replace("\"", "");
    }



}

