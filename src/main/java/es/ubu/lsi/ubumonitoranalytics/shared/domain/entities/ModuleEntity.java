package es.ubu.lsi.ubumonitoranalytics.shared.domain.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "modules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ModuleEntity extends AuditableEntity {

    @Id
    private Integer id;

    private Integer courseId;

    private String name;

    private String modName;

    @Lob
    private String url;

    @Lob
    private String description;

    private Boolean visible;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private SectionEntity section;
}
