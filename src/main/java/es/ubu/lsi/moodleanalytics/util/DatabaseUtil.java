package es.ubu.lsi.moodleanalytics.util;

import java.net.IDN;
import java.net.URI;

public class DatabaseUtil {
    public static String toHostString(URI host) {
        if (host == null) {
            return null;
        }
        return IDN.toASCII(host.getHost());
    }
}
