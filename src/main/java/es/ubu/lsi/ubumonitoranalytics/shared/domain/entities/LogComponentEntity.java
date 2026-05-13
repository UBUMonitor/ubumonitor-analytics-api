package es.ubu.lsi.ubumonitoranalytics.shared.domain.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "logs_components")
@Getter
@Setter
@NoArgsConstructor
public class LogComponentEntity {
    @Id
    private Byte id;


    private String name;
}
