package org.example.servinet.core.application.usecase.api;

import org.example.servinet.core.domain.repository.EmailSenderProvider;

public class EmailUseCase {
    private final EmailSenderProvider emailSenderProvider;

    public EmailUseCase(EmailSenderProvider emailSenderProvider) {
        this.emailSenderProvider = emailSenderProvider;
    }

    public void sendEmail(String to, String subject, String body) {
        emailSenderProvider.sendEmail(to, subject, body);
    }
}