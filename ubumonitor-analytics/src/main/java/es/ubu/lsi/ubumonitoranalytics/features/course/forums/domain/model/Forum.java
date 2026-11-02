package es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Forum {
  private Long id;
  private Long courseId;
  private Long moduleId;
  private String name;
  private String intro;
  private String type;
  private Boolean visible;
  private String groupMode;
  private Long groupingId;
  private Long dueDate;
  private Long cutoffDate;
  private String assessed;
  private Integer scale;
  private Integer gradeForum;
  private Long maxBytes;
  private Integer maxAttachments;
  private String forceSubscribe;
  private String trackingType;
  private Long warnAfter;
  private Long blockAfter;
  private Long blockPeriod;
  private Long lockDiscussionAfter;
  private Integer completionDiscussions;
  private Integer completionReplies;
  private Integer completionPosts;
  private Long timeModified;
  private List<ForumDiscussion> discussions;
}
