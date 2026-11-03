package es.ubu.lsi.ubumonitoranalytics.features.course.forums.infrastructure.out.moodle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.api.mod.forum.ModForumApi;
import es.ubu.lsi.moodle.model.mod.forum.getdiscussionposts.request.GetDiscussionPostsRequestApi;
import es.ubu.lsi.moodle.model.mod.forum.getdiscussionposts.response.GetDiscussionPostsResponseApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumdiscussions.request.GetForumDiscussionsRequestApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumdiscussions.response.DiscussionApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumdiscussions.response.GetForumDiscussionsResponseApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumsbycourses.request.GetForumsByCoursesRequestApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumsbycourses.response.GetForumsByCoursesResponseApi;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.concurrent.AsyncOperations;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class FetchCourseForumsAdapterTest {

  @Test
  void fetchesDiscussionPostsInParallel() {
    Client client = mock(Client.class);
    ModForumApi forumApi = mock(ModForumApi.class);
    when(client.modForum()).thenReturn(forumApi);

    GetForumsByCoursesResponseApi forum = new GetForumsByCoursesResponseApi();
    forum.setId(10L);
    forum.setCmid(100L);
    forum.setCourse(1L);
    forum.setName("Forum");
    forum.setType("general");
    when(forumApi.getForumsByCourses(any(GetForumsByCoursesRequestApi.class)))
        .thenReturn(List.of(forum));

    DiscussionApi firstDiscussion = new DiscussionApi();
    firstDiscussion.setDiscussion(101L);
    firstDiscussion.setName("First");
    DiscussionApi secondDiscussion = new DiscussionApi();
    secondDiscussion.setDiscussion(102L);
    secondDiscussion.setName("Second");

    GetForumDiscussionsResponseApi discussions = new GetForumDiscussionsResponseApi();
    discussions.setDiscussions(List.of(firstDiscussion, secondDiscussion));
    when(forumApi.getForumDiscussions(any(GetForumDiscussionsRequestApi.class)))
        .thenReturn(discussions);

    CountDownLatch bothCallsStarted = new CountDownLatch(2);
    when(forumApi.getDiscussionPosts(any(GetDiscussionPostsRequestApi.class)))
        .thenAnswer(
            invocation -> {
              bothCallsStarted.countDown();
              if (!bothCallsStarted.await(2, TimeUnit.SECONDS)) {
                throw new AssertionError("Discussion-post requests were not started concurrently");
              }
              return new GetDiscussionPostsResponseApi();
            });

    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      FetchCourseForumsAdapter adapter =
          new FetchCourseForumsAdapter(
              client, new CourseForumsMoodleMapperImpl(), new AsyncOperations(executor));

      var result = adapter.fetchCourseForums(1L);

      assertThat(result.getForums()).hasSize(1);
      assertThat(result.getForums().getFirst().getDiscussions()).hasSize(2);
      assertThat(result.getForums().getFirst().getDiscussions())
          .allSatisfy(discussion -> assertThat(discussion.getPosts()).isEmpty());
    }
  }
}
