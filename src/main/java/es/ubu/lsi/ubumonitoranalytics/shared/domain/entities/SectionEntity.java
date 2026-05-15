package es.ubu.lsi.ubumonitoranalytics.shared.domain.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity course;

    private String name;

    @Lob
    private String summary;

    private Integer position;

    private Boolean visible;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL)
    @OrderBy("position ASC")
    private List<ModuleEntity> modules = new ArrayList<>();

    private Boolean active;

    public void addModule(ModuleEntity module) {
        modules.add(module);
        module.setSection(this);
    }
}
