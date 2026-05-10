package es.ubu.lsi.ubumonitoranalytics.features.course.content.domain.model;

import lombok.Data;

import java.net.URI;

@Data
public class CourseModule {

    private Integer id;


    private String name;

    private String modName;

    private URI url;

    private String description;

    private Boolean visible;
}


