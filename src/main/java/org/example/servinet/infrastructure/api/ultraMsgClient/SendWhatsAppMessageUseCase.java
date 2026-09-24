package org.example.servinet.infrastructure.api.ultraMsgClient;

import org.example.servinet.core.application.dto.WhatsAppDto;
import org.example.servinet.core.domain.exception.ApiException;
import org.example.servinet.core.domain.repository.WhatsAppSendProvider;
import org.example.servinet.infrastructure.database.config.ConfigLoad;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class SendWhatsAppMessageUseCase implements WhatsAppSendProvider {

    private final HttpClient client = HttpClient.newHttpClient();

    public SendWhatsAppMessageUseCase(){}

    public String sendMessage(String phone, String message, WhatsAppDto whatsAppDto){


        String body =
                "token=" + encode(whatsAppDto.getToken()) +
                        "&to=" + encode(phone) +
                        "&body=" + encode(message);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "https://api.ultramsg.com/"
                                    + whatsAppDto.getInstance()
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
        } catch (IOException | InterruptedException e){
            throw new ApiException("Error al  conectar la api con WhatsApp", e);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }
}
