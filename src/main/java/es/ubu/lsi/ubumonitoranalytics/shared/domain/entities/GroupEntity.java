package es.ubu.lsi.ubumonitoranalytics.shared.domain.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupEntity extends AuditableEntity {

    @Id
    private Integer id;
    private String name;
    private String description;

}
