package es.ubu.lsi.ubumonitoranalytics.features.enrollmentcourse.domain.model;

import lombok.Data;

@Data
public class UserRole {
    private User user;
    private Role role;
}
