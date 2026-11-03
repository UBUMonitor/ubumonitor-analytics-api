package es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ForumDiscussion {
  private Long id;
  private Long forumId;
  private String name;
  private Long groupId;
  private Long userId;
  private Long timeStart;
  private Long timeEnd;
  private Long timeModified;
  private Boolean pinned;
  private Boolean locked;
  private List<ForumPost> posts;
}
