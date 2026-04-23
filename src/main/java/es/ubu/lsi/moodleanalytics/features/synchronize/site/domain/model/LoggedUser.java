package es.ubu.lsi.moodleanalytics.features.synchronize.site.domain.model;


import es.ubu.lsi.moodleanalytics.shared.domain.model.UserPicture;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoggedUser {
    private Long moodleId;
    private String userName;
    private String fullName;
    private String firstName;
    private String lastName;
    private UserPicture userPicture;


}
