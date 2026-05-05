package es.ubu.lsi.ubumonitoranalytics.shared.domain.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.URI;

@Entity
@Table(name = "sites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteEntity  extends AuditableEntity {
    @Id
    private Integer id;

    private URI host;

    private String siteName;

    private String versionNumber;

    private String typeOfLogin;

    private URI launchUrl;

    private Integer userId;
    private String userName;
    private String fullName;
    private String firstName;
    private String lastName;


    private URI userImageUrl;
}

