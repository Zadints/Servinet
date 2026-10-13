package org.example.servinet.core.domain.entities.technicians;

public class Order {
    private String orderId;
    private String clientName;
    private String planId;
    private String clientAddress ;
    private String orderStatus;
    private String orderStartInstall;
    private String orderEndInstall;


    public String getOrderId() {
        return orderId;
    }

    public String getClientName() {
        return clientName;
    }

    public String getPlanId() {
        return planId;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public String getOrderStartInstall() {
        return orderStartInstall;
    }

    public String getOrderEndInstall() {
        return orderEndInstall;
    }
}
