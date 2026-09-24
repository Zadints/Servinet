package org.example.servinet.core.application.usecase.api;

import org.example.servinet.core.application.dto.DniDataDto;
import org.example.servinet.core.domain.repository.DniProvider;

public class ConsultDniUseCase {
    private final DniProvider dniProvider;

    public ConsultDniUseCase(DniProvider dniProvider) {
        this.dniProvider = dniProvider;
    }

    public DniDataDto execute(String dni) {
        return dniProvider.consultar(dni);
    }
}
