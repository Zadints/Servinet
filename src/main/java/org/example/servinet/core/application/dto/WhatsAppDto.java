package org.example.servinet.core.application.dto;

public class WhatsAppDto {
    private String token;
    private String instance;

    public WhatsAppDto(String instance, String token) {
        this.instance = instance;
        this.token = token;
    }

    public String getInstance() {
        return instance;
    }

    public String getToken() {
        return token;
    }


}
