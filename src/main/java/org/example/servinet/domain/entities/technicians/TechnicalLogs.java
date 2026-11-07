package org.example.servinet.domain.entities.technicians;

public record TechnicalLogs (
        String serviceUuid,
        String staffUuid,
        String clientDni,
        String actionType,
        String orderId,
        String description,
        boolean currentOrder
){

}
