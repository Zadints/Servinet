package org.example.servinet.core.domain.utils;
import java.util.UUID;
public class UuidGenerate {
    public static String getNewUuid(){
        return UUID.randomUUID().toString();
    }
}
