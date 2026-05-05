package es.ubu.lsi.ubumonitoranalytics.shared.domain.entities;


import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleId implements Serializable {

    private Integer userId;

    private Integer roleId;

    private Integer courseId;
}
