package org.example.servinet.application.service;
import org.example.servinet.domain.repository.IdGeneratorRepository;

import java.util.UUID;
public class UuidGenerator implements IdGeneratorRepository {

    @Override
    public String generate(){
        return UUID.randomUUID().toString();
    }
}
