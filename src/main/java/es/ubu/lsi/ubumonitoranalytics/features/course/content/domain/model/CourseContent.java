package es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class CourseContent {
    private Integer courseId;
    private List<Section> sections;

}


