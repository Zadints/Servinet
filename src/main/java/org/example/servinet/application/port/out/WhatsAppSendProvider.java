package org.example.servinet.application.port.out;

public interface WhatsAppSendProvider {
    String sendMessage(String phone, String message);
}
