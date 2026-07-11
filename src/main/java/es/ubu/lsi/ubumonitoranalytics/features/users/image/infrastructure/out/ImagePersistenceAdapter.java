package es.ubu.lsi.ubumonitoranalytics.features.users.image.infrastructure.out;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersImages.USERS_IMAGES;

import es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.out.ImagePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.model.UserImage;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.UsersImagesRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImagePersistenceAdapter implements ImagePersistencePort {

  private final Jooq jooq;
  private final UserImageMapper userImageMapper;

  @Override
  public String getImageHash(Integer userId) {
    return jooq.dsl()
        .select(USERS_IMAGES.IMAGE_HASH)
        .from(USERS_IMAGES)
        .where(USERS_IMAGES.USER_ID.eq(userId))
        .fetchOne(USERS_IMAGES.IMAGE_HASH);
  }

  @Override
  public UserImage fetchUserImage(Integer userId) {
    UsersImagesRecord dto = jooq.dsl().fetchOne(USERS_IMAGES, USERS_IMAGES.USER_ID.eq(userId));

    return userImageMapper.toDomain(dto);
  }
}
