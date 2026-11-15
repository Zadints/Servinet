package org.example.servinet.infrastructure.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.servinet.application.dto.DniDataDto;
import org.example.servinet.domain.exception.ApiException;
import org.example.servinet.application.port.out.DniGetProvider;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApisPeruAdapter implements DniGetProvider {

    private final HttpClient httpClient;
    private String token;
    private final ObjectMapper mapper;

    public ApisPeruAdapter(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.mapper = new ObjectMapper();
    }


    @Override
    public void setApiPeruConfig(String token){
        this.token = token;
    }

    @Override
    public DniDataDto consultar(String dni) {

        if (token == null){
            throw new ApiException("No se puede consultar el dni porque no se estableció el dni");
        }

        String url = "https://dniruc.apisperu.com/api/v1/dni/"
                + dni + "?token=" + token;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new ApiException(
                        "API Perú respondió con código: " + response.statusCode()
                );
            }

            JsonNode json = mapper.readTree(response.body());

            if (json.isNull() || json.isEmpty()) {
                return null;
            }

            if (json.has("success") && !json.get("success").asBoolean()) {
                return null;
            }
            ((com.fasterxml.jackson.databind.node.ObjectNode) json)
                    .remove("success");

            ((com.fasterxml.jackson.databind.node.ObjectNode) json)
                    .remove("codVerificaLetra");

            return mapper.treeToValue(json, DniDataDto.class);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("La consulta del DNI fue interrumpida", e);

        } catch (IOException e) {
            throw new ApiException("Error al obtener respuesta de la API Perú", e);
        }
    }
}
