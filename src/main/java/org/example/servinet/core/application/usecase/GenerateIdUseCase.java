package org.example.servinet.core.application.usecase;

import org.example.servinet.core.domain.repository.IdGenerator;

public class GenerateIdUseCase {
    private final IdGenerator idGenerator;

    public GenerateIdUseCase(IdGenerator generador) {
        this.idGenerator = generador;
    }

    public String execute(){
        return idGenerator.generate();
    }

}
