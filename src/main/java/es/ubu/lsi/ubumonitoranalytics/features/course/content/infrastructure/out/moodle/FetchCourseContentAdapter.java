package es.ubu.lsi.ubumonitoranalytics.features.course.content.infrastructure.out.moodle;

import es.ubu.lsi.moodleadapter.api.generated.api.CoursesApi;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterCourseContentsResponseDto;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterGetCourseContentsOptionsParameterDto;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.application.port.out.FetchCourseContentPort;
import es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model.CourseContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FetchCourseContentAdapter implements FetchCourseContentPort {

    private final CoursesApi coursesApi;
    private final FetchCourseContentAdapaterMapper fetchCourseContentAdapaterMapper;

    @Override
    public CourseContent fetchCourseContent(Integer courseId) {
        MoodleAdapterCourseContentsResponseDto dto = coursesApi.getCourseContents(courseId, new MoodleAdapterGetCourseContentsOptionsParameterDto());
        return fetchCourseContentAdapaterMapper.toDomain(dto, courseId);
    }
}


