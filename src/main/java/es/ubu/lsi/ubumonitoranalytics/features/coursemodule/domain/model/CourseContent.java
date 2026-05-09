package es.ubu.lsi.ubumonitoranalytics.features.coursemodule.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class CourseContent {
    private Integer id;
    private List<Section> sections;
    private List<CourseModule> modules;
}
