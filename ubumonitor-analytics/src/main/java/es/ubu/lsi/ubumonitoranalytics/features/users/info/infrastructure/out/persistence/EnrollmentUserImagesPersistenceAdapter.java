package es.ubu.lsi.ubumonitoranalytics.features.users.info.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Users.USERS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.UsersImages.USERS_IMAGES;

import es.ubu.lsi.ubumonitoranalytics.features.users.info.appication.port.out.EnrollmentUserImagesPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.document.EnrollmentImagesDocumentCommand;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.domain.model.image.EnrollmentUserImage;
import es.ubu.lsi.ubumonitoranalytics.features.users.info.infrastructure.out.persistence.mapper.EnrollmentUserImagesMapper;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EnrollmentUserImagesPersistenceAdapter implements EnrollmentUserImagesPersistencePort {
  private final Jooq jooq;
  private final EnrollmentUserImagesMapper enrollmentUserImagesMapper;

  @Override
  public List<EnrollmentUserImage> getCourseEnrollmentImages(
      EnrollmentImagesDocumentCommand command) {
    List<Long> userIds = command.getUserIds();

    Map<Long, EnrollmentUserImage> byId =
        jooq
            .dsl()
            .select(USERS.ID, USERS.FULL_NAME, USERS_IMAGES.IMAGE_DATA, USERS_IMAGES.CONTENT_TYPE)
            .from(USERS)
            .join(USERS_IMAGES)
            .on(USERS.ID.eq(USERS_IMAGES.USER_ID))
            .where(USERS.ID.in(userIds))
            .fetch()
            .stream()
            .collect(Collectors.toMap(r -> r.get(USERS.ID), enrollmentUserImagesMapper::toDomain));

    return userIds.stream().map(byId::get).filter(Objects::nonNull).toList();
  }

  @Override
  public List<EnrollmentUserImage> getCourseEnrollmentImages(
      EnrollmentImagesDocumentCommand command, byte[] imageData, String contentType) {

    List<Long> userIds = command.getUserIds();

    Map<Long, EnrollmentUserImage> byId =
        jooq
            .dsl()
            .select(USERS.ID, USERS.FULL_NAME)
            .from(USERS)
            .where(USERS.ID.in(userIds))
            .fetch()
            .stream()
            .collect(
                Collectors.toMap(
                    r -> r.get(USERS.ID),
                    r -> enrollmentUserImagesMapper.toDomain(r, imageData, contentType)));

    return userIds.stream().map(byId::get).filter(Objects::nonNull).toList();
  }
}
