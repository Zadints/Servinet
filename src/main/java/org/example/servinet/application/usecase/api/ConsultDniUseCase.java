package org.example.servinet.application.usecase.api;

import org.example.servinet.application.dto.DniDataDto;
import org.example.servinet.application.port.out.DniGetProvider;

public class ConsultDniUseCase {
    private final DniGetProvider dniProvider;

    public ConsultDniUseCase(DniGetProvider dniProvider) {
        this.dniProvider = dniProvider;
    }

    public DniDataDto execute(String dni) {
        return dniProvider.consultar(dni);
    }
}
