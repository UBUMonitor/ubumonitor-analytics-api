package es.ubu.lsi.moodleanalytics.features.synchronize.site.infrastructure.in.rest;

import es.ubu.lsi.moodleanalytics.api.generated.api.SyncApiDelegate;
import es.ubu.lsi.moodleanalytics.api.generated.model.SyncSiteResponseDto;
import es.ubu.lsi.moodleanalytics.features.synchronize.site.application.port.in.SiteInfoUseCase;
import es.ubu.lsi.moodleanalytics.features.synchronize.site.domain.model.SiteInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncApiDelegateImpl implements SyncApiDelegate {

    private final SiteInfoUseCase siteInfoUseCase;
    private final SyncApiDelegateMapperImpl syncApiDelegateMapper;
    @Override
    public ResponseEntity<SyncSiteResponseDto> syncSiteInfo() {
        SiteInfo siteInfo = siteInfoUseCase.sync();
        return ResponseEntity.ok(syncApiDelegateMapper.toDto(siteInfo));
    }
}
