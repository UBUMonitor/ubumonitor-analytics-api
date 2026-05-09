package es.ubu.lsi.ubumonitoranalytics.shared.domain.entities;

import jakarta.persistence.Basic;
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
@Table(name = "users_images")
@Getter
@Setter
public class UserImageEntity extends AuditableEntity{

    @Id
    private Integer userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] imageData;

    private String imageHash;

}
