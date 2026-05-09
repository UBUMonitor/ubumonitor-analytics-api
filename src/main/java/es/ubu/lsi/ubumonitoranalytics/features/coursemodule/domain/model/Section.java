package es.ubu.lsi.ubumonitoranalytics.features.coursemodule.domain.model;


import lombok.Data;

import java.util.List;

@Data
public class Section {

    private Integer id;

    private Integer courseId;

    private Integer position;

    private String name;

    private String summary;

    private Boolean visible;

    private List<Integer> moduleIds;
}
