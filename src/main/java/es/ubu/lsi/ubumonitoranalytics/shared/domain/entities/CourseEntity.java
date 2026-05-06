package es.ubu.lsi.ubumonitoranalytics.shared.domain.entities;


import jakarta.persistence.CascadeType;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseEntity extends AuditableEntity {

    @Id
    private Integer id;

    private String fullName;
    private String shortName;
    private Boolean showGrades;
    private Boolean enableCompletion;

    private OffsetDateTime startDate;
    private OffsetDateTime endDate;

    private OffsetDateTime timeModified;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<UserCourseEntity> userCourses = new ArrayList<>();
}
