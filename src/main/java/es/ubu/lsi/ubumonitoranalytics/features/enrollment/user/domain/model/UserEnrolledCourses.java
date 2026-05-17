package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class UserEnrolledCourses {
    private Integer userId;
    private List<Enrollment> enrolledCourses;
}

