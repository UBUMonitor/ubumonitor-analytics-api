package es.ubu.lsi.moodle.api.mod.forum;

import es.ubu.lsi.moodle.api.Client;
import es.ubu.lsi.moodle.model.mod.forum.getdiscussionposts.request.GetDiscussionPostsRequestApi;
import es.ubu.lsi.moodle.model.mod.forum.getdiscussionposts.response.GetDiscussionPostsResponseApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumdiscussions.request.GetForumDiscussionsRequestApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumdiscussions.response.GetForumDiscussionsResponseApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumsbycourses.request.GetForumsByCoursesRequestApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumsbycourses.response.GetForumsByCoursesResponseApi;
import java.util.List;
import lombok.RequiredArgsConstructor;

/** Implements Moodle forum calls through the shared client. */
@RequiredArgsConstructor
public class ModForumApiImpl implements ModForumApi {
  private final Client client;

  @Override
  public GetForumDiscussionsResponseApi getForumDiscussions(GetForumDiscussionsRequestApi request) {
    return client.execute(request, GetForumDiscussionsResponseApi.class);
  }

  @Override
  public GetDiscussionPostsResponseApi getDiscussionPosts(GetDiscussionPostsRequestApi request) {
    return client.execute(request, GetDiscussionPostsResponseApi.class);
  }

  @Override
  public List<GetForumsByCoursesResponseApi> getForumsByCourses(
      GetForumsByCoursesRequestApi request) {
    return client.executeList(request, GetForumsByCoursesResponseApi.class);
  }
}
