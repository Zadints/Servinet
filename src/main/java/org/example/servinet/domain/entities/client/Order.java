package org.example.servinet.domain.entities.client;

import org.example.servinet.domain.entities.Antenna;
import org.example.servinet.domain.enums.OrderStatus;

import java.time.LocalDateTime;

public class Order {
    private final String orderId;

    private String antennaUuid;
    private String planUuid;

    private LocalDateTime orderCreated;
    private OrderStatus orderStatus;

    public Order(String orderId, String antennaUuid, String planUuid, LocalDateTime orderCreated, OrderStatus orderStatus) {
        this.orderId = orderId;
        this.antennaUuid = antennaUuid;
        this.planUuid = planUuid;
        this.orderCreated = orderCreated;
        this.orderStatus = orderStatus;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getAntennaUuid() {
        return antennaUuid;
    }

    public void setAntennaUuid(String antennaUuid) {
        this.antennaUuid = antennaUuid;
    }

    public String getPlanUuid() {
        return planUuid;
    }

    public void setPlanUuid(String planUuid) {
        this.planUuid = planUuid;
    }

    public LocalDateTime getOrderCreated() {
        return orderCreated;
    }

    public void setOrderCreated(LocalDateTime orderCreated) {
        this.orderCreated = orderCreated;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }


}
