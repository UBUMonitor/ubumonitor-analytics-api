package es.ubu.lsi.ubumonitoranalytics.features.coursemodule.domain.model;

import lombok.Data;

import java.net.URI;

@Data
public class CourseModule {

    private Integer id;

    private Integer courseId;

    private Integer sectionId;

    private String name;

    private String modName;

    private URI url;

    private String description;

    private Boolean visible;
}
