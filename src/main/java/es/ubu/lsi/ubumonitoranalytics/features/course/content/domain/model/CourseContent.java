package es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Data
@Builder
public class CourseContent {
    private Integer courseId;
    private Collection<Section> sections;

}


