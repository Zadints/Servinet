package org.example.servinet.infrastructure.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.servinet.application.dto.DniDataDto;
import org.example.servinet.domain.exception.ApiException;
import org.example.servinet.application.port.out.DniGetProvider;
import org.example.servinet.infrastructure.database.DatabaseFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApisPeruAdapter implements DniGetProvider {

    private final HttpClient httpClient;
    private final String token;
    private final ObjectMapper mapper;

    public ApisPeruAdapter(HttpClient httpClient, String token) {
        this.httpClient = httpClient;
        this.token = token;
        this.mapper = new ObjectMapper();
    }

    public DniDataDto consultar(String dni) {

        String url = "https://dniruc.apisperu.com/api/v1/dni/" + dni  + "?token=" + token;
        System.out.println(dni);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            System.out.println("URL: " + url);
            System.out.println("TOKEN: " + token);
            System.out.println("STATUS: " + response.statusCode());
            System.out.println("BODY: " + response.body());

            if (response.statusCode() != 200) {
                throw new ApiException(
                        "API Perú respondió con código: " + response.statusCode()
                );
            }

            JsonNode json = mapper.readTree(response.body());

            if (json.has("success") && !json.get("success").asBoolean()) {
                throw new ApiException(json.get("message").asText());
            }

            return mapper.treeToValue(json, DniDataDto.class);

        } catch (IOException | InterruptedException e) {
            throw new ApiException("Error al obtener respuesta de la api perú",e);
        }

    }
}
