package org.example.servinet.application.port.out;

public interface WhatsAppSendProvider {
    String sendMessage(String phone, String message);

    String sendDocument(
            String phone,
            byte[] document,
            String filename,
            String caption
    );
}
