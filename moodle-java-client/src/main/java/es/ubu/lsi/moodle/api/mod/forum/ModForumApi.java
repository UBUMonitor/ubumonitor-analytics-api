package es.ubu.lsi.moodle.api.mod.forum;

import es.ubu.lsi.moodle.model.mod.forum.getdiscussionposts.request.GetDiscussionPostsRequestApi;
import es.ubu.lsi.moodle.model.mod.forum.getdiscussionposts.response.GetDiscussionPostsResponseApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumdiscussions.request.GetForumDiscussionsRequestApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumdiscussions.response.GetForumDiscussionsResponseApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumsbycourses.request.GetForumsByCoursesRequestApi;
import es.ubu.lsi.moodle.model.mod.forum.getforumsbycourses.response.GetForumsByCoursesResponseApi;
import java.util.List;

/** Exposes Moodle forum operations. */
public interface ModForumApi {
  /**
   * @param request query for discussions in a forum
   * @return forum discussions
   */
  GetForumDiscussionsResponseApi getForumDiscussions(GetForumDiscussionsRequestApi request);

  /**
   * @param request query for posts in a discussion
   * @return discussion posts
   */
  GetDiscussionPostsResponseApi getDiscussionPosts(GetDiscussionPostsRequestApi request);

  /**
   * @param request query for forums in one or more courses
   * @return matching forums
   */
  List<GetForumsByCoursesResponseApi> getForumsByCourses(GetForumsByCoursesRequestApi request);
}
