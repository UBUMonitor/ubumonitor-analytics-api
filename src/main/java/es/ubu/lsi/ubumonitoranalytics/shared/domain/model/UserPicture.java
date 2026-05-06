package es.ubu.lsi.ubumonitoranalytics.shared.domain.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPicture {
    private URI url;
    private byte[] data;
    private String hexHash;
}
