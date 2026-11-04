package org.example.servinet.domain.entities.technicians;

public class TechnicalLogs {
    private  final  String servicioUuid;
    private  final String clientDni;
    private final  String accionTipo;
    private  final String orderId;
    private  final String description;

    public TechnicalLogs(String servicioUuid, String clientDni, String accionTipo, String orderId, String description) {
        this.servicioUuid = servicioUuid;
        this.clientDni = clientDni;
        this.accionTipo = accionTipo;
        this.orderId = orderId;
        this.description = description;
    }

    public String getServicioUuid() {
        return servicioUuid;
    }

    public String getClientDni() {
        return clientDni;
    }

    public String getAccionTipo() {
        return accionTipo;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getDescription() {
        return description;
    }
}
