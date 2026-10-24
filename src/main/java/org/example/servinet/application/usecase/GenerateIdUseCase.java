package org.example.servinet.application.usecase;

import org.example.servinet.domain.repository.IdGeneratorRepository;

public class GenerateIdUseCase {
    private final IdGeneratorRepository idGenerator;

    public GenerateIdUseCase(IdGeneratorRepository generador) {
        this.idGenerator = generador;
    }

    public String execute(){
        return idGenerator.generate();
    }

}
