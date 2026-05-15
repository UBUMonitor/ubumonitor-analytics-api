package es.ubu.lsi.ubumonitoranalytics.features.users.image.domain.model;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserImage {
    private byte[] image;
    private String hexHash;
    private boolean isModified;
    private String contentType;
}

