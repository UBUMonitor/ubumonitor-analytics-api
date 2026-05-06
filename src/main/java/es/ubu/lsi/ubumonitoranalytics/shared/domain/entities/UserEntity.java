package es.ubu.lsi.ubumonitoranalytics.shared.domain.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class UserEntity extends AuditableEntity {

    @Id
    private Integer id;


    private String fullName;
    private String firstName;
    private String lastName;
    private String email;
    private OffsetDateTime firstAccess;
    private OffsetDateTime lastAccess;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserImageEntity image;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserCourseEntity> userCourses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserGroupEntity> userGroups = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserRoleEntity> userRoles = new ArrayList<>();


}
