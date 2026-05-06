package es.ubu.lsi.ubumonitoranalytics.features.users.image.infrastructure.out;


import es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.out.ImagePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImagePersistenceAdapter implements ImagePersistencePort {
    private final UserImageRepository userImageRepository;


    @Override
    public String getImageHash(Integer userId) {
        return userImageRepository.findImageHashByUserId(userId);
    }

    @Override
    public byte[] fetchUserImage(Integer userId) {
        return userImageRepository.findImageDataByUserId(userId);
    }
}
