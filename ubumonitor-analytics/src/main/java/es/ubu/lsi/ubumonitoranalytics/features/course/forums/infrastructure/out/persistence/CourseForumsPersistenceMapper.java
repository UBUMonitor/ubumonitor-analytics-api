package es.ubu.lsi.ubumonitoranalytics.features.course.forums.infrastructure.out.persistence;

import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.Forum;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.ForumDiscussion;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.ForumPost;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.ForumsDiscussionsRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.ForumsPostsRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.ForumsRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = GlobalMapperConfig.class)
public interface CourseForumsPersistenceMapper {

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", source = "now")
  @Mapping(target = "active", constant = "true")
  @Mapping(target = "assessed", source = "forum.assessed", qualifiedByName = "assessedCode")
  @Mapping(
      target = "forceSubscribe",
      source = "forum.forceSubscribe",
      qualifiedByName = "forceSubscribeCode")
  @Mapping(
      target = "trackingType",
      source = "forum.trackingType",
      qualifiedByName = "trackingTypeCode")
  @Mapping(target = "groupMode", source = "forum.groupMode", qualifiedByName = "groupModeCode")
  ForumsRecord toRecord(Forum forum, Long now);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", source = "now")
  @Mapping(target = "active", constant = "true")
  ForumsDiscussionsRecord toRecord(ForumDiscussion discussion, Long now);

  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", source = "now")
  @Mapping(target = "active", constant = "true")
  ForumsPostsRecord toRecord(ForumPost post, Long now);

  @Mapping(target = "assessed", source = "assessed", qualifiedByName = "assessedName")
  @Mapping(
      target = "forceSubscribe",
      source = "forceSubscribe",
      qualifiedByName = "forceSubscribeName")
  @Mapping(target = "trackingType", source = "trackingType", qualifiedByName = "trackingTypeName")
  @Mapping(target = "groupMode", source = "groupMode", qualifiedByName = "groupModeName")
  @Mapping(target = "discussions", ignore = true)
  Forum toDomain(ForumsRecord record);

  @Mapping(target = "posts", ignore = true)
  ForumDiscussion toDomain(ForumsDiscussionsRecord record);

  ForumPost toDomain(ForumsPostsRecord record);

  @Named("assessedCode")
  default Integer assessedCode(String assessed) {
    if (assessed == null) {
      return null;
    }
    return switch (assessed) {
      case "AVERAGE" -> 1;
      case "COUNT" -> 2;
      case "MAX" -> 3;
      case "MIN" -> 4;
      case "SUM" -> 5;
      default ->
          throw new IllegalArgumentException("Unsupported forum assessed value: " + assessed);
    };
  }

  @Named("assessedName")
  default String assessedName(Integer assessed) {
    if (assessed == null) {
      return null;
    }
    return switch (assessed) {
      case 1 -> "AVERAGE";
      case 2 -> "COUNT";
      case 3 -> "MAX";
      case 4 -> "MIN";
      case 5 -> "SUM";
      default ->
          throw new IllegalArgumentException("Unsupported forum assessed value: " + assessed);
    };
  }

  @Named("forceSubscribeCode")
  default Integer forceSubscribeCode(String forceSubscribe) {
    if (forceSubscribe == null) {
      return null;
    }
    return switch (forceSubscribe) {
      case "OPTIONAL" -> 0;
      case "FORCED" -> 1;
      case "AUTO" -> 2;
      case "DISABLED" -> 3;
      default ->
          throw new IllegalArgumentException(
              "Unsupported forum subscription mode: " + forceSubscribe);
    };
  }

  @Named("forceSubscribeName")
  default String forceSubscribeName(Integer forceSubscribe) {
    if (forceSubscribe == null) {
      return null;
    }
    return switch (forceSubscribe) {
      case 0 -> "OPTIONAL";
      case 1 -> "FORCED";
      case 2 -> "AUTO";
      case 3 -> "DISABLED";
      default ->
          throw new IllegalArgumentException(
              "Unsupported forum subscription mode: " + forceSubscribe);
    };
  }

  @Named("trackingTypeCode")
  default Integer trackingTypeCode(String trackingType) {
    if (trackingType == null) {
      return null;
    }
    return switch (trackingType) {
      case "OFF" -> 0;
      case "OPTIONAL" -> 1;
      case "FORCED" -> 2;
      default ->
          throw new IllegalArgumentException("Unsupported forum tracking type: " + trackingType);
    };
  }

  @Named("trackingTypeName")
  default String trackingTypeName(Integer trackingType) {
    if (trackingType == null) {
      return null;
    }
    return switch (trackingType) {
      case 0 -> "OFF";
      case 1 -> "OPTIONAL";
      case 2 -> "FORCED";
      default ->
          throw new IllegalArgumentException("Unsupported forum tracking type: " + trackingType);
    };
  }

  @Named("groupModeCode")
  default Integer groupModeCode(String groupMode) {
    if (groupMode == null) {
      return null;
    }
    return switch (groupMode) {
      case "NO_GROUPS" -> 0;
      case "SEPARATE_GROUPS" -> 1;
      case "VISIBLE_GROUPS" -> 2;
      default -> throw new IllegalArgumentException("Unsupported forum group mode: " + groupMode);
    };
  }

  @Named("groupModeName")
  default String groupModeName(Integer groupMode) {
    if (groupMode == null) {
      return null;
    }
    return switch (groupMode) {
      case 0 -> "NO_GROUPS";
      case 1 -> "SEPARATE_GROUPS";
      case 2 -> "VISIBLE_GROUPS";
      default -> throw new IllegalArgumentException("Unsupported forum group mode: " + groupMode);
    };
  }
}
