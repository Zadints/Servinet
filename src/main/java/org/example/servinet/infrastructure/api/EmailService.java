package org.example.servinet.infrastructure.api;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {


    private final String remitente = "servinet.soporte@gmail.com"; // Tu correo o el del sistema
    private final String password = "tu-contraseña-de-aplicacion"; // Tu contraseña de aplicación de Google

    public void enviarCorreoContrato(String destinatario, String nombreCliente, String plan) {
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
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            message.setSubject("Contrato de Internet Servinet Aprobado");
            message.setText("Hola " + nombreCliente + ",\n\nTu contrato para el plan " + plan + " ha sido registrado exitosamente en Servinet.\n\n¡Gracias por confiar en nosotros!");

            Transport.send(message);
            System.out.println("Correo enviado exitosamente a " + destinatario);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

}
