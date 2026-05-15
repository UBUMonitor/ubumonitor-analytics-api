package es.ubu.lsi.ubumonitoranalytics.features.enrollment.course.domain.model;

import lombok.Data;

@Data
public class UserRole {
    private User user;
    private Role role;
}

