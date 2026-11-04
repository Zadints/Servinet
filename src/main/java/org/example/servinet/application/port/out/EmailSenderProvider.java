package org.example.servinet.application.port.out;

public interface EmailSenderProvider {
    void sendEmail(String to, String subject, String body);

    void sendEmail(
            String to,
            String subject,
            String body,
            byte[] attachment,
            String filename
    );
}
