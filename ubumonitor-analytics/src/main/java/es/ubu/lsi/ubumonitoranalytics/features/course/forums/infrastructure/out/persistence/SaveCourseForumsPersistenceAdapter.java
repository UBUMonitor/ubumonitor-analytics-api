package es.ubu.lsi.ubumonitoranalytics.features.course.forums.infrastructure.out.persistence;

import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.Forums.FORUMS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.ForumsDiscussions.FORUMS_DISCUSSIONS;
import static es.ubu.lsi.ubumonitoranalytics.jooq.tables.ForumsPosts.FORUMS_POSTS;

import es.ubu.lsi.ubumonitoranalytics.features.course.forums.application.port.out.SaveCourseForumsPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.CourseForums;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.Forum;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.ForumDiscussion;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.ForumPost;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.ForumsDiscussionsRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.ForumsPostsRecord;
import es.ubu.lsi.ubumonitoranalytics.jooq.tables.records.ForumsRecord;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.database.Jooq;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.UpdatableRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SaveCourseForumsPersistenceAdapter implements SaveCourseForumsPort {

  private final Jooq jooq;
  private final CourseForumsPersistenceMapper mapper;

  @Override
  @Transactional
  public void save(CourseForums courseForums) {
    Long courseId = courseForums.getCourseId();
    Long now = Instant.now().getEpochSecond();
    deactivateExisting(courseId, now);

    List<ForumsRecord> forums = new ArrayList<>();
    List<ForumsDiscussionsRecord> discussions = new ArrayList<>();
    List<ForumsPostsRecord> posts = new ArrayList<>();

    if (courseForums.getForums() != null) {
      for (Forum forum : courseForums.getForums()) {
        forums.add(mapper.toRecord(forum, now));
        if (forum.getDiscussions() == null) {
          continue;
        }
        for (ForumDiscussion discussion : forum.getDiscussions()) {
          discussions.add(mapper.toRecord(discussion, now));
          if (discussion.getPosts() == null) {
            continue;
          }
          for (ForumPost post : discussion.getPosts()) {
            posts.add(mapper.toRecord(post, now));
          }
        }
      }
    }

    merge(forums);
    merge(discussions);
    merge(posts);
  }

  private void deactivateExisting(Long courseId, Long now) {
    jooq.dsl()
        .update(FORUMS_POSTS)
        .set(FORUMS_POSTS.ACTIVE, false)
        .set(FORUMS_POSTS.UPDATED_AT, now)
        .where(FORUMS_POSTS.ACTIVE.isTrue())
        .andExists(
            jooq.dsl()
                .selectOne()
                .from(FORUMS_DISCUSSIONS)
                .join(FORUMS)
                .on(FORUMS.ID.eq(FORUMS_DISCUSSIONS.FORUM_ID))
                .where(FORUMS_DISCUSSIONS.ID.eq(FORUMS_POSTS.DISCUSSION_ID))
                .and(FORUMS.COURSE_ID.eq(courseId)))
        .execute();

    jooq.dsl()
        .update(FORUMS_DISCUSSIONS)
        .set(FORUMS_DISCUSSIONS.ACTIVE, false)
        .set(FORUMS_DISCUSSIONS.UPDATED_AT, now)
        .where(FORUMS_DISCUSSIONS.ACTIVE.isTrue())
        .andExists(
            jooq.dsl()
                .selectOne()
                .from(FORUMS)
                .where(FORUMS.ID.eq(FORUMS_DISCUSSIONS.FORUM_ID))
                .and(FORUMS.COURSE_ID.eq(courseId)))
        .execute();

    jooq.dsl()
        .update(FORUMS)
        .set(FORUMS.ACTIVE, false)
        .set(FORUMS.UPDATED_AT, now)
        .where(FORUMS.COURSE_ID.eq(courseId))
        .and(FORUMS.ACTIVE.isTrue())
        .execute();
  }

  private void merge(List<? extends UpdatableRecord<?>> records) {
    if (!records.isEmpty()) {
      jooq.dsl().batchMerge(records).execute();
    }
  }
}
