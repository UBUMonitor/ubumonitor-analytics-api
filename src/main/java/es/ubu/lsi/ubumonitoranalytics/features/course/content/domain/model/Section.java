package es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model;


import lombok.Data;

import java.util.List;

@Data
public class Section {

    private Integer id;
    private Integer position;
    private String name;
    private String summary;
    private Boolean visible;
    private List<CourseModule> modules;
}


