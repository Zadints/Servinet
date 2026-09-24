package org.example.servinet.core.domain.repository;

import org.example.servinet.core.application.dto.WhatsAppDto;

public interface WhatsAppSendProvider {
    String sendMessage(String phone, String message , WhatsAppDto whatsAppDto);
}
