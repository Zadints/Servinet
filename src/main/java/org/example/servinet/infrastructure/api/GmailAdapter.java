package org.example.servinet.infrastructure.api;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.activation.DataHandler;
import jakarta.mail.util.ByteArrayDataSource;

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
        send(to, subject, body, null, null);
    }

    @Override
    public void sendEmail(
            String to,
            String subject,
            String body,
            byte[] attachment,
            String filename
    ) {
        send(to, subject, body, attachment, filename);
    }

    private void send(
            String to,
            String subject,
            String body,
            byte[] attachment,
            String filename
    ) {

        Properties props = new Properties();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        gmailConfig.gmail(),
                        gmailConfig.password()
                );
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(gmailConfig.gmail()));

            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to)
            );

            message.setSubject(subject, "UTF-8");

            if (attachment != null && attachment.length > 0) {

                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(body, "UTF-8");

                MimeBodyPart attachmentPart = new MimeBodyPart();

                ByteArrayDataSource source = new ByteArrayDataSource(
                        attachment,
                        "application/pdf"
                );

                attachmentPart.setDataHandler(new DataHandler(source));
                attachmentPart.setFileName(filename);

                Multipart multipart = new MimeMultipart();

                multipart.addBodyPart(textPart);
                multipart.addBodyPart(attachmentPart);

                message.setContent(multipart);

            } else {
                message.setText(body, "UTF-8");
            }

            Transport.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException(
                    "Error al enviar el correo electrónico: " + e.getMessage(),
                    e
            );
        }
    }
}