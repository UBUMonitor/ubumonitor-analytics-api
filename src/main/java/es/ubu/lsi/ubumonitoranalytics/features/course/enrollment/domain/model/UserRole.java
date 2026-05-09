package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model;

import lombok.Data;

@Data
public class UserRole {
    private User user;
    private Role role;
}
