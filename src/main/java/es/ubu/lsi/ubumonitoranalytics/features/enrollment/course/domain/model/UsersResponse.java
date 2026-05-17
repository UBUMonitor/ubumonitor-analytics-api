package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Data
@Builder
public class UsersResponse {
    private Integer actualCourseId;
    private Collection<User> users;
}
