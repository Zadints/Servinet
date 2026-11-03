package org.example.servinet.infrastructure.api;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.example.servinet.application.port.out.EmailSenderProvider;
import org.example.servinet.infrastructure.dto.GmailConfig;

import java.util.Properties;

public class GmailAdapter implements EmailSenderProvider {

    private final GmailConfig gmailConfig;

    public GmailAdapter(GmailConfig gmailConfig) {
        this.gmailConfig = gmailConfig;
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(gmailConfig.gmail(), gmailConfig.password());
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(gmailConfig.gmail()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("✅ Correo enviado exitosamente a " + to);

        } catch (MessagingException e) {
            throw new RuntimeException("❌ Error al enviar el correo electrónico: " + e.getMessage(), e);
        }
    }
}
