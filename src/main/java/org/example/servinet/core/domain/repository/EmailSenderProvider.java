package org.example.servinet.core.domain.repository;

public interface EmailSenderProvider {
    void sendEmail(String to, String subject, String body);
}