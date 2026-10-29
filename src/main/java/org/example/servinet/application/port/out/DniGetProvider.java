package org.example.servinet.application.port.out;

import org.example.servinet.application.dto.DniDataDto;

public interface DniGetProvider {
    DniDataDto consultar(String dni);
}
