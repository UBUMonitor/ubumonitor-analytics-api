package es.ubu.lsi.ubumonitoranalytics.features.course.forums.infrastructure.out.moodle;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.mod.forum.getdiscussionposts.request.GetDiscussionPostsArgsApi;
import es.ubu.lsi.moodle.model.mod.forum.getdiscussionposts.request.GetDiscussionPostsRequestApi;
import es.ubu.lsi.moodle.model.mod.forum.getdiscussionposts.response.GetDiscussionPostsResponseApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumdiscussions.request.GetForumDiscussionsArgsApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumdiscussions.request.GetForumDiscussionsRequestApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumdiscussions.response.DiscussionApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumdiscussions.response.GetForumDiscussionsResponseApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumsbycourses.request.GetForumsByCoursesArgsApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumsbycourses.request.GetForumsByCoursesRequestApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumsbycourses.response.GetForumsByCoursesResponseApi;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.application.port.out.FetchCourseForumsPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.CourseForums;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.Forum;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.ForumDiscussion;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.ForumPost;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.concurrent.AsyncOperations;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FetchCourseForumsAdapter implements FetchCourseForumsPort {

  private final Client client;
  private final CourseForumsMoodleMapper mapper;
  private final AsyncOperations asyncOperations;

  @Override
  public CourseForums fetchCourseForums(Long courseId) {
    GetForumsByCoursesArgsApi args = new GetForumsByCoursesArgsApi();
    args.setCourseids(List.of(Math.toIntExact(courseId)));
    GetForumsByCoursesRequestApi request = new GetForumsByCoursesRequestApi();
    request.setArgs(args);

    List<GetForumsByCoursesResponseApi> response = client.modForum().getForumsByCourses(request);
    if (response == null) {
      return CourseForums.builder().courseId(courseId).forums(List.of()).build();
    }

    List<Forum> forums = new ArrayList<>();
    List<ForumDiscussion> discussions = new ArrayList<>();
    List<CompletableFuture<List<ForumPost>>> postRequests = new ArrayList<>();

    for (GetForumsByCoursesResponseApi forumResponse : response) {
      Forum forum = mapper.toForum(forumResponse, courseId);
      GetForumDiscussionsArgsApi discussionArgs = new GetForumDiscussionsArgsApi();
      discussionArgs.setForumid(forum.getId());
      GetForumDiscussionsRequestApi discussionRequest = new GetForumDiscussionsRequestApi();
      discussionRequest.setArgs(discussionArgs);

      GetForumDiscussionsResponseApi discussionsResponse =
          client.modForum().getForumDiscussions(discussionRequest);
      List<DiscussionApi> forumDiscussions =
          discussionsResponse == null ? null : discussionsResponse.getDiscussions();
      List<ForumDiscussion> mappedDiscussions = new ArrayList<>();

      if (forumDiscussions != null) {
        for (DiscussionApi discussionResponse : forumDiscussions) {
          ForumDiscussion discussion = mapper.toDiscussion(discussionResponse, forum.getId());
          discussions.add(discussion);
          mappedDiscussions.add(discussion);
          postRequests.add(fetchDiscussionPostsAsync(discussion.getId()));
        }
      }

      forum.setDiscussions(mappedDiscussions);
      forums.add(forum);
    }

    CompletableFuture.allOf(postRequests.toArray(CompletableFuture[]::new)).join();
    for (int i = 0; i < discussions.size(); i++) {
      discussions.get(i).setPosts(postRequests.get(i).join());
    }

    return CourseForums.builder().courseId(courseId).forums(forums).build();
  }

  private CompletableFuture<List<ForumPost>> fetchDiscussionPostsAsync(Long discussionId) {
    return asyncOperations.supplyAsync(
        () -> {
          GetDiscussionPostsArgsApi args = new GetDiscussionPostsArgsApi();
          args.setDiscussionid(discussionId);
          GetDiscussionPostsRequestApi request = new GetDiscussionPostsRequestApi();
          request.setArgs(args);

          GetDiscussionPostsResponseApi response = client.modForum().getDiscussionPosts(request);
          return mapper.toPosts(response == null ? null : response.getPosts(), discussionId);
        });
  }
}
