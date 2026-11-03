package es.ubu.lsi.ubumonitoranalytics.features.course.forums.infrastructure.out.moodle;

import es.ubu.lsi.moodle.model.mod.forum.getdiscussionposts.response.AttachmentApi;
import es.ubu.lsi.moodle.model.mod.forum.getdiscussionposts.response.PostApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumdiscussions.response.DiscussionApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumsbycourses.response.GetForumsByCoursesResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.Forum;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.ForumDiscussion;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.ForumPost;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import java.util.List;
import java.util.Locale;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = GlobalMapperConfig.class)
public interface CourseForumsMoodleMapper {

  @Mapping(target = "courseId", source = "courseId")
  @Mapping(target = "moduleId", source = "source.cmid")
  @Mapping(target = "type", source = "source.type", qualifiedByName = "forumType")
  @Mapping(target = "visible", constant = "true")
  @Mapping(target = "groupMode", ignore = true)
  @Mapping(target = "groupingId", ignore = true)
  @Mapping(target = "dueDate", source = "source.duedate")
  @Mapping(target = "cutoffDate", source = "source.cutoffdate")
  @Mapping(target = "assessed", source = "source.assessed", qualifiedByName = "assessedName")
  @Mapping(target = "gradeForum", source = "source.gradeForum")
  @Mapping(target = "maxBytes", source = "source.maxbytes")
  @Mapping(target = "maxAttachments", source = "source.maxattachments")
  @Mapping(
      target = "forceSubscribe",
      source = "source.forcesubscribe",
      qualifiedByName = "forceSubscribeName")
  @Mapping(
      target = "trackingType",
      source = "source.trackingtype",
      qualifiedByName = "trackingTypeName")
  @Mapping(target = "warnAfter", source = "source.warnafter")
  @Mapping(target = "blockAfter", source = "source.blockafter")
  @Mapping(target = "blockPeriod", source = "source.blockperiod")
  @Mapping(target = "lockDiscussionAfter", source = "source.lockdiscussionafter")
  @Mapping(target = "completionDiscussions", source = "source.completiondiscussions")
  @Mapping(target = "completionReplies", source = "source.completionreplies")
  @Mapping(target = "completionPosts", source = "source.completionposts")
  @Mapping(target = "timeModified", source = "source.timemodified")
  @Mapping(target = "discussions", ignore = true)
  Forum toForum(GetForumsByCoursesResponseApi source, Long courseId);

  @Mapping(target = "id", source = "source.discussion")
  @Mapping(target = "forumId", source = "forumId")
  @Mapping(target = "groupId", source = "source.groupid")
  @Mapping(target = "userId", source = "source.userid")
  @Mapping(target = "timeStart", source = "source.timestart")
  @Mapping(target = "timeEnd", source = "source.timeend")
  @Mapping(target = "timeModified", source = "source.timemodified")
  @Mapping(target = "pinned", source = "source.pinned", qualifiedByName = "booleanValue")
  @Mapping(target = "locked", source = "source.locked", qualifiedByName = "booleanValue")
  @Mapping(target = "posts", ignore = true)
  ForumDiscussion toDiscussion(DiscussionApi source, Long forumId);

  default List<ForumPost> toPosts(List<PostApi> sources, Long discussionId) {
    if (sources == null) {
      return List.of();
    }
    return sources.stream().map(source -> toPost(source, discussionId)).toList();
  }

  @Mapping(target = "discussionId", source = "discussionId")
  @Mapping(target = "userId", source = "source.author.id")
  @Mapping(
      target = "messageFormat",
      source = "source.messageformat",
      qualifiedByName = "messageFormat")
  @Mapping(
      target = "hasAttachment",
      source = "source.attachments",
      qualifiedByName = "hasAttachments")
  @Mapping(target = "isDeleted", source = "source.isdeleted")
  @Mapping(target = "isPrivateReply", source = "source.isprivatereply")
  @Mapping(target = "parentId", source = "source.parentid")
  @Mapping(target = "timeCreated", source = "source.timecreated")
  @Mapping(target = "timeModified", source = "source.timemodified")
  ForumPost toPost(PostApi source, Long discussionId);

  @Named("forumType")
  default String forumType(String type) {
    return type == null ? null : type.toUpperCase(Locale.ROOT);
  }

  @Named("assessedName")
  default String assessedName(Integer assessed) {
    if (assessed == null || assessed == 0) {
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

  @Named("forceSubscribeName")
  default String forceSubscribeName(Boolean forced) {
    if (forced == null) {
      return null;
    }
    return forced ? "FORCED" : "OPTIONAL";
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

  @Named("booleanValue")
  default Boolean booleanValue(Boolean value) {
    return Boolean.TRUE.equals(value);
  }

  @Named("messageFormat")
  default Integer messageFormat(PostApi.Messageformat messageFormat) {
    return messageFormat == null ? null : messageFormat.value();
  }

  @Named("hasAttachments")
  default Boolean hasAttachments(List<AttachmentApi> attachments) {
    return attachments != null && !attachments.isEmpty();
  }
}
