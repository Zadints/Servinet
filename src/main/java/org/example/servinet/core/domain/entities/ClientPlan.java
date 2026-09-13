package org.example.servinet.core.domain.entities;

public class ClientPlan {

    private final String id;
    private String name;
    private Integer speed;
    private Double price;
    private Boolean active;

    public ClientPlan(
            String id,
            String name,
            Integer speed,
            Double price,
            Boolean active
    ) {
        this.id = id;
        this.name = name;
        this.speed = speed;
        this.price = price;
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
}
