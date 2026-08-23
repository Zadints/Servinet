package org.example.mineide.utils;
import java.util.UUID;
public class IdGenerate {
    public static String getNewId(){
        return UUID.randomUUID().toString();
    }
}
