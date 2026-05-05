package es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.application.port.in.GetUserCoursesUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.enrollmentuser.domain.model.UserCourses;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class GetUserCoursesService implements GetUserCoursesUseCase {

    @Override
    public UserCourses getActualUserEnrollments() {
        return null;
    }
}
