package es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.domain.model;

import lombok.Data;

import java.util.List;


@Data
public class UserCourses {
    private Integer userId;
    private List<Course> courses;
}
