package org.example.servinet.application.port.out;

import org.example.servinet.application.dto.ClientDto;

import java.io.IOException;

public interface PdfProvider {
    byte[] generate(ClientDto dto) throws IOException;
}
