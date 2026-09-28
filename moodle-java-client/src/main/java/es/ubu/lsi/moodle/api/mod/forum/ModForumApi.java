package es.ubu.lsi.moodle.api.mod.forum;

import es.ubu.lsi.moodle.model.mod.forum.getdiscussionposts.request.GetDiscussionPostsRequestApi;
import es.ubu.lsi.moodle.model.mod.forum.getdiscussionposts.response.GetDiscussionPostsResponseApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumdiscussions.request.GetForumDiscussionsRequestApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumdiscussions.response.GetForumDiscussionsResponseApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumsbycourses.request.GetForumsByCoursesRequestApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumsbycourses.response.GetForumsByCoursesResponseApi;
import java.util.List;

public interface ModForumApi {
  GetForumDiscussionsResponseApi getForumDiscussions(GetForumDiscussionsRequestApi request);

  GetDiscussionPostsResponseApi getDiscussionPosts(GetDiscussionPostsRequestApi request);

  List<GetForumsByCoursesResponseApi> getForumsByCourses(GetForumsByCoursesRequestApi request);
}
