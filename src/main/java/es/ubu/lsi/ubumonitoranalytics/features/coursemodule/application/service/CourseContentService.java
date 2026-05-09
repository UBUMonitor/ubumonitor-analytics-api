package es.ubu.lsi.ubumonitoranalytics.features.coursemodule.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.coursemodule.application.port.in.SyncCourseModulesUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.coursemodule.application.port.out.CourseContentPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.coursemodule.domain.model.CourseContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseContentService implements SyncCourseModulesUseCase {

    private final CourseContentPersistencePort persistencePort;

    @Transactional
    @Override
    public void syncCourseContent(CourseContent content) {
        persistencePort.save(content);
    }


}
