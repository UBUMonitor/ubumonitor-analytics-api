package es.ubu.lsi.ubumonitoranalytics.features.course.forums.infrastructure.in.rest;

import es.ubu.lsi.ubumonitoranalytics.api.generated.model.CourseForumsResponseDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.ForumDiscussionDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.ForumDto;
import es.ubu.lsi.ubumonitoranalytics.api.generated.model.ForumPostDto;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.CourseForums;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.Forum;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.ForumDiscussion;
import es.ubu.lsi.ubumonitoranalytics.features.course.forums.domain.model.ForumPost;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.mapper.GlobalMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface CourseForumsApiDelegateMapper {

  CourseForumsResponseDto toDto(CourseForums source);

  ForumDto toDto(Forum source);

  ForumDiscussionDto toDto(ForumDiscussion source);

  ForumPostDto toDto(ForumPost source);
}
