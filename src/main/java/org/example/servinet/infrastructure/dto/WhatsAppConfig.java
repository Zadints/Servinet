package org.example.servinet.infrastructure.dto;

public class WhatsAppConfig {
    private String token;
    private String instance;

    public WhatsAppConfig(String instance, String token) {
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
