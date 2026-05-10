package es.ubu.lsi.ubumonitoranalytics.features.users.image.appication.port.out;



public interface ImagePersistencePort {
    String getImageHash(Integer userId);
    byte[] fetchUserImage(Integer userId);
}

