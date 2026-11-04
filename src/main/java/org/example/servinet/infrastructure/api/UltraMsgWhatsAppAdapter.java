package org.example.servinet.infrastructure.api;

import org.example.servinet.infrastructure.dto.WhatsAppConfig;
import org.example.servinet.domain.exception.ApiException;
import org.example.servinet.application.port.out.WhatsAppSendProvider;
import java.util.Base64;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class UltraMsgWhatsAppAdapter implements WhatsAppSendProvider {

    private final HttpClient client;
    private final WhatsAppConfig whatsAppConfig;

    public UltraMsgWhatsAppAdapter(HttpClient client, WhatsAppConfig whatsAppConfig) {
        this.client = client;
        this.whatsAppConfig = whatsAppConfig;
    }

    public String sendMessage(String phone, String message){

        System.out.println(whatsAppConfig.getToken() + "   " + whatsAppConfig.getInstance());

        String body =
                "token=" + encode(whatsAppConfig.getToken()) +
                        "&to=" + encode(phone) +
                        "&body=" + encode(message);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "https://api.ultramsg.com/"
                                    + whatsAppConfig.getInstance()
                                    + "/messages/chat"
                    ))
                    .header(
                            "Content-Type",
                            "application/x-www-form-urlencoded"
                    )
                    .POST(
                            HttpRequest.BodyPublishers.ofString(body)
                    )
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 ||
                    response.statusCode() >= 300) {

                throw new RuntimeException(
                        "Error HTTP " +
                                response.statusCode() +
                                ": " +
                                response.body()
                );
            }

            return response.body();
        } catch (Exception e){
            System.out.println(e.getMessage()+ e.getCause());
            throw new ApiException("Error al  conectar la api con WhatsApp", e);
        }
    }

    @Override
    public String sendDocument(
            String phone,
            byte[] document,
            String filename,
            String caption
    ) {
        if (document == null || document.length == 0) {
            throw new IllegalArgumentException("El documento está vacío");
        }

        if (filename == null || filename.isBlank() || filename.length() > 255) {
            throw new IllegalArgumentException("Nombre de archivo inválido");
        }

        String base64 = Base64.getEncoder().encodeToString(document);

        if (base64.length() > 10_000_000) {
            throw new IllegalArgumentException(
                    "El documento supera el límite de Base64 de UltraMsg"
            );
        }

        String body =
                "token=" + encode(whatsAppConfig.getToken()) +
                        "&to=" + encode(phone) +
                        "&filename=" + encode(filename) +
                        "&document=" + encode(base64) +
                        "&caption=" + encode(caption != null ? caption : "");

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "https://api.ultramsg.com/"
                                    + whatsAppConfig.getInstance()
                                    + "/messages/document"
                    ))
                    .header(
                            "Content-Type",
                            "application/x-www-form-urlencoded"
                    )
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApiException(
                        "Error HTTP " + response.statusCode() +
                                ": " + response.body()
                );
            }

            return response.body();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Envío de documento interrumpido", e);

        } catch (IOException e) {
            throw new ApiException("Error al enviar documento por WhatsApp", e);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }
}
