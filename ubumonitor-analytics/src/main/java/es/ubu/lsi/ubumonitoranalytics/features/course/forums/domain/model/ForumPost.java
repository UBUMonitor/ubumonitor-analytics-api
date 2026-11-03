package es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForumPost {
  private Long id;
  private Long discussionId;
  private Long parentId;
  private Long userId;
  private String subject;
  private String message;
  private Integer messageFormat;
  private Boolean hasAttachment;
  private Boolean isDeleted;
  private Boolean isPrivateReply;
  private Long timeCreated;
  private Long timeModified;
}
