package es.ubu.lsi.ubumonitoranalytics.shared.domain.entities;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
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

    private String userName;
    private String fullName;
    private String firstName;
    private String lastName;
    private String email;
    private Instant firstAccess;
    private Instant lastAccess;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserImageEntity image;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCourseEntity> userCourses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserGroupEntity> userGroups = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRoleEntity> userRoles = new ArrayList<>();


    public void setImageData(byte[] data) {
        if (data == null) {
            this.image = null;
            return;
        }

        if (this.image == null) {
            this.image = new UserImageEntity();
            this.image.setUser(this);
        }
        this.image.setData(data);
    }
}
