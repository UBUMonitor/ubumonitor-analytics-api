package es.ubu.lsi.ubumonitoranalytics.shared.domain.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.URI;

    @Entity
    @Table(name = "modules")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public class ModuleEntity extends AuditableEntity {

        @Id
        private Integer id;


        private String name;

        private String modName;

        private URI url;

        @Lob
        private String description;

        private Boolean visible;

        private Boolean active;

        private Integer position;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "section_id", nullable = false)
        private SectionEntity section;
    }
