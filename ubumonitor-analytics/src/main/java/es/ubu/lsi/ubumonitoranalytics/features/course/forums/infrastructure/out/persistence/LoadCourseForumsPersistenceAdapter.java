package es.ubu.lsi.ubumonitoranalytics.features.course.forums.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Forums.FORUMS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.ForumsDiscussions.FORUMS_DISCUSSIONS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.ForumsPosts.FORUMS_POSTS;

import es.ubu.lsi.ubumonitoranalytics.features.course.forums.application.port.out.LoadCourseForumsPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.CourseForums;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.Forum;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.ForumDiscussion;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.ForumPost;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoadCourseForumsPersistenceAdapter implements LoadCourseForumsPort {

  private final Jooq jooq;
  private final CourseForumsPersistenceMapper mapper;

  @Override
  public CourseForums load(Long courseId) {
    Map<Long, Forum> forums =
        jooq.dsl()
            .selectFrom(FORUMS)
            .where(FORUMS.COURSE_ID.eq(courseId))
            .and(FORUMS.ACTIVE.isTrue())
            .orderBy(FORUMS.ID)
            .fetchMap(FORUMS.ID, mapper::toDomain);

    if (forums.isEmpty()) {
      return CourseForums.builder().courseId(courseId).forums(List.of()).build();
    }

    Map<Long, List<ForumDiscussion>> discussionsByForum =
        jooq.dsl()
            .selectFrom(FORUMS_DISCUSSIONS)
            .where(FORUMS_DISCUSSIONS.FORUM_ID.in(forums.keySet()))
            .and(FORUMS_DISCUSSIONS.ACTIVE.isTrue())
            .orderBy(FORUMS_DISCUSSIONS.ID)
            .fetchGroups(FORUMS_DISCUSSIONS.FORUM_ID, mapper::toDomain);

    List<ForumDiscussion> discussions =
        discussionsByForum.values().stream().flatMap(List::stream).toList();
    if (discussions.isEmpty()) {
      for (Forum forum : forums.values()) {
        forum.setDiscussions(List.of());
      }
      return CourseForums.builder().courseId(courseId).forums(List.copyOf(forums.values())).build();
    }

    Map<Long, List<ForumPost>> postsByDiscussion =
        jooq.dsl()
            .selectFrom(FORUMS_POSTS)
            .where(
                FORUMS_POSTS.DISCUSSION_ID.in(
                    discussions.stream().map(ForumDiscussion::getId).toList()))
            .and(FORUMS_POSTS.ACTIVE.isTrue())
            .orderBy(FORUMS_POSTS.ID)
            .fetchGroups(FORUMS_POSTS.DISCUSSION_ID, mapper::toDomain);

    for (ForumDiscussion discussion : discussions) {
      discussion.setPosts(postsByDiscussion.getOrDefault(discussion.getId(), List.of()));
    }
    for (Forum forum : forums.values()) {
      forum.setDiscussions(discussionsByForum.getOrDefault(forum.getId(), List.of()));
    }

    return CourseForums.builder().courseId(courseId).forums(List.copyOf(forums.values())).build();
  }
}
