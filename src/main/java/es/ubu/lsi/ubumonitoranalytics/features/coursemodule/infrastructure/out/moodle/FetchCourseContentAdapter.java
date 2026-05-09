package es.ubu.lsi.ubumonitoranalytics.features.coursemodule.infrastructure.out.moodle;

import es.ubu.lsi.moodleadapter.api.generated.api.CoursesApi;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterCourseContentsResponseDto;
import es.ubu.lsi.moodleadapter.api.generated.model.MoodleAdapterGetCourseContentsOptionsParameterDto;
import es.ubu.lsi.ubumonitoranalytics.features.coursemodule.application.port.out.FetchCourseContent;
import es.ubu.lsi.ubumonitoranalytics.features.coursemodule.domain.model.CourseContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FetchCourseContentAdapter implements FetchCourseContent {

    private final CoursesApi coursesApi;
    private final FetchCourseContentAdapaterMapper fetchCourseContentAdapaterMapper;

    @Override
    public CourseContent fetchCourseContent(Integer courseId) {
        MoodleAdapterCourseContentsResponseDto dto = coursesApi.getCourseContents(courseId, new MoodleAdapterGetCourseContentsOptionsParameterDto());
        return fetchCourseContentAdapaterMapper.toDomain(dto);
    }
}
