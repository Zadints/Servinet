package org.example.servinet.domain.entities.client;

import java.time.LocalDateTime;

public class ClientPlan {

    private final String id;
    private String name;
    private LocalDateTime createdAt;
    private Integer speed;
    private Double price;
    private Boolean isPromo;
    private Boolean active;

    public ClientPlan(String id, String name, LocalDateTime createdAt, Integer speed, Double price, Boolean isPromo, Boolean active) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.speed = speed;
        this.price = price;
        this.isPromo = isPromo;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getSpeed() {
        return speed;
    }

    public Double getPrice() {
        return price;
    }

    public Boolean getActive() {
        return active;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Boolean getPromo() {
        return isPromo;
    }

    @Override
    public String toString() {
        if (name == null) return "";
        return name + " (" + (speed != null ? speed : 0) + " Mbps) - S/ " + String.format("%.2f", price != null ? price : 0.0);
    }
}
