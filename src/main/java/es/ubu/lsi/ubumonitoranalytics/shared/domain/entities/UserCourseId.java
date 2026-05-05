package es.ubu.lsi.ubumonitoranalytics.shared.domain.entities;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;

import lombok.Builder;
import lombok.Data;

import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCourseId implements Serializable {

    private Integer userId;

    private Integer courseId;

}

