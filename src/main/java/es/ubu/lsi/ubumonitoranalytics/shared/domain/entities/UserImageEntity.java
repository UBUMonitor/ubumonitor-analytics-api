package es.ubu.lsi.ubumonitoranalytics.shared.domain.entities;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "user_images")
@Getter
@Setter
public class UserImageEntity {

    @Id
    private Integer userId; // Usaremos el mismo ID del usuario

    @OneToOne
    @MapsId // Comparte la PK con la entidad User
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "image_data")
    private byte[] data;
}
