package es.ubu.lsi.ubumonitoranalytics.features.users.image.infrastructure.out;


import es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.out.ImagePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.model.UserImage;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.persistence.repository.UserImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImagePersistenceAdapter implements ImagePersistencePort {
    private final UserImageRepository userImageRepository;
    private final UserImageMapper userImageMapper;

    @Override
    public String getImageHash(Integer userId) {
        return userImageRepository.findImageHashByUserId(userId);
    }

    @Override
    public UserImage fetchUserImage(Integer userId) {
        return userImageMapper.toDomain(userImageRepository.findById(userId).orElse(null));
    }
}

