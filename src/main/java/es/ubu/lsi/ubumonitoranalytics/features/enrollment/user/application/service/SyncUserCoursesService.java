package es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.service;

import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.in.SyncUserCoursesUseCase;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out.CoursePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out.EnrollmentCoursesFetchPort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out.EnrollmentPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.application.port.out.UserPersistencePort;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.Course;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.Enrollment;
import es.ubu.lsi.ubumonitoranalytics.features.enrollment.user.domain.model.UserEnrolledCourses;
import es.ubu.lsi.ubumonitoranalytics.shared.application.port.out.persistence.SitePersistencePort;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.exception.EntityNotFoundException;
import es.ubu.lsi.ubumonitoranalytics.shared.domain.model.SessionData;
import es.ubu.lsi.ubumonitoranalytics.shared.infrastructure.session.CurrentSessionContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SyncUserCoursesService implements SyncUserCoursesUseCase {
    private final CurrentSessionContext currentSessionContext;
    private final EnrollmentCoursesFetchPort enrollmentCoursesFetchPort;
    private final SitePersistencePort sitePersistencePort;
    private final UserPersistencePort userPort;
    private final CoursePersistencePort coursePort;
    private final EnrollmentPersistencePort enrollmentPort;

    @Override
    @Transactional
    public UserEnrolledCourses syncActualUserEnrollments() {
        SessionData sessionData = currentSessionContext.getSessionData();
        Integer userId = sitePersistencePort.getActualUserId(sessionData.getUserName());
        if (userId == null) {
            throw new EntityNotFoundException("User not found " + sessionData.getUserName());
        }
        UserEnrolledCourses userEnrolledCourses = enrollmentCoursesFetchPort.fetchEnrolledCourses(userId);

        final List<Enrollment> enrollments = userEnrolledCourses.getEnrolledCourses();
        final List<Course> courses = enrollments.stream()
            .map(Enrollment::getCourse)
            .toList();

        userPort.findOrCreate(userId);           // 1. Asegurar usuario
        coursePort.saveAll(courses);             // 2. Upsert cursos
        enrollmentPort.sync(userId, enrollments); // 3. Sync matrículas

        return userEnrolledCourses;
    }




}

