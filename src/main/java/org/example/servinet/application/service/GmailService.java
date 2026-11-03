package org.example.servinet.application.service;

import org.example.servinet.application.port.out.EmailSenderProvider;

public class GmailService {
    private final EmailSenderProvider emailSenderProvider;

    public GmailService(EmailSenderProvider emailSenderProvider) {
        this.emailSenderProvider = emailSenderProvider;
    }

/**
 * @param to es destino
 * @param subject es remitente
 * @param body es el contenido
 **/

    public void sendEmail(String to, String subject, String body) {
        emailSenderProvider.sendEmail(to, subject, body);
    }
}
