package org.example.servinet.application.port.out.api;

import org.example.servinet.infrastructure.dto.WhatsAppConfig;

public interface WhatsAppSendProvider {
    String sendMessage(String phone, String message);
    void setWhatsAppConfig(WhatsAppConfig whatsAppConfig);
    String sendDocument(
            String phone,
            byte[] document,
            String filename,
            String caption
    );
    boolean isSetCredential();
}
