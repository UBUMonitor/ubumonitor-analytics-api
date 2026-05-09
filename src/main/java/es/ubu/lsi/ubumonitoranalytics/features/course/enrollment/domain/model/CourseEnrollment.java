package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model;

import lombok.Data;

import java.util.List;

@Data
public class CourseEnrollment {
    private Integer courseId;
    private List<Enrollment> enrollments;

}
