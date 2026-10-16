package org.example.servinet.infrastructure.email;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.example.servinet.core.domain.repository.EmailSenderProvider;

import java.util.Properties;

public class GmailSenderImpl implements EmailSenderProvider {

    // ⚠️ REEMPLAZA "tucorreo@gmail.com" CON TU CORREO REAL DE GMAIL
    private final String remitente = "tucorreo@gmail.com";
    private final String password = "jzce qcyd dlqj clxt";  // Tu contraseña de aplicación

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
                return new PasswordAuthentication(remitente, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(remitente));
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