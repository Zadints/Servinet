package org.example.servinet.application.usecase.api;

import org.example.servinet.application.dto.DniDataDto;
import org.example.servinet.domain.provider.DniProvider;

public class ConsultDniUseCase {
    private final DniProvider dniProvider;

    public ConsultDniUseCase(DniProvider dniProvider) {
        this.dniProvider = dniProvider;
    }

    public DniDataDto execute(String dni) {
        return dniProvider.consultar(dni);
    }
}
