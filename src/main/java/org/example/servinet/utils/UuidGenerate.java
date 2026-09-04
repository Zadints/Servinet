package org.example.servinet.utils;
import java.util.UUID;
public class UuidGenerate {
    public static String getNewUuid(){
        return UUID.randomUUID().toString();
    }
}
