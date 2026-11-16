package org.example.servinet.application.port.out.api;

import org.example.servinet.application.dto.DniDataDto;

public interface DniGetProvider {
    DniDataDto consultar(String dni);
    void setApiPeruConfig(String token);
}
