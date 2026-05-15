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
public class UserGroupId implements Serializable {

    private Integer userId;

    private Integer groupId;

    private Integer courseId;
}

