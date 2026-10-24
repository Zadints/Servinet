package org.example.servinet.domain.provider;

import org.example.servinet.application.dto.DniDataDto;

public interface DniProvider {
    DniDataDto consultar(String dni);
}
