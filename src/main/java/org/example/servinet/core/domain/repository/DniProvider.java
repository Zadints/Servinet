package org.example.servinet.core.domain.repository;

import org.example.servinet.core.application.dto.DniDataDto;

public interface DniProvider {
    DniDataDto consultar(String dni);
}
