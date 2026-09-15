package org.example.servinet.core.application.service;
import org.example.servinet.core.domain.repository.IdGenerator;

import java.util.UUID;
public class UuidGenerator implements IdGenerator {

    @Override
    public static String generate(){
        return UUID.randomUUID().toString();
    }
}
