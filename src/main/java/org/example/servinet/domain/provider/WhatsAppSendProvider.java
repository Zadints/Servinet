package org.example.servinet.domain.provider;

import org.example.servinet.application.dto.WhatsAppDto;

public interface WhatsAppSendProvider {
    String sendMessage(String phone, String message , WhatsAppDto whatsAppDto);
}
