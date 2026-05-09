package es.ubu.lsi.ubumonitoranalytics.shared.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SectionEntity extends AuditableEntity {

    @Id
    private Integer id;

    @Column(name = "course_id", nullable = false)
    private Integer courseId;

    private String name;

    @Lob
    private String summary;

    private Integer position;

    private Boolean visible;

    @OneToMany(mappedBy = "section")
    private List<ModuleEntity> modules;

    private Boolean active;
}
