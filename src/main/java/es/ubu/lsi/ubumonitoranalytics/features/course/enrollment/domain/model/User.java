package es.ubu.lsi.ubumonitoranalytics.features.course.enrollment.domain.model;

import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.UserPicture;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class User {
    private Integer id;
    private String fullName;
    private String firstName;
    private String lastName;
    private String email;
    private OffsetDateTime firstAccess;
    private OffsetDateTime lastAccess;
    private UserPicture userPicture;
    private List<Role> roles;
    private List<Group> groups;
    private List<Course> courses;

}
