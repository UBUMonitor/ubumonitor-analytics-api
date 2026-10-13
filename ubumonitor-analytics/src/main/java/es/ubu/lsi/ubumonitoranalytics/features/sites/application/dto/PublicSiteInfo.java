package es.ubu.lsi.ubumonitoranalytics.features.sites.application.dto;

import es.ubu.lsi.ubumonitoranalytics.features.sites.domain.model.Site;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicSiteInfo {
  private Site site;
}
