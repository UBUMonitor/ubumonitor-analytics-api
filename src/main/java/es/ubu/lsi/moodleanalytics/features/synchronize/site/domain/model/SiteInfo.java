package es.ubu.lsi.moodleanalytics.features.synchronize.site.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteInfo {
    private Site site;
    private LoggedUser loggedUser;
}
